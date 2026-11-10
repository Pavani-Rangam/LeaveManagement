package com.teamhub.leave.web;

import com.teamhub.leave.model.*;
import com.teamhub.leave.repo.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

/** Home, Inbox (notifications), Calendar, Employees and Settings pages. */
@Controller
public class PagesController {
    private final Auth auth;
    private final EmployeeRepository employees;
    private final LeaveRequestRepository leaves;
    private final NotificationRepository notifications;
    private final AttendanceRepository attendances;
    private final PasswordEncoder encoder;

    public PagesController(Auth auth, EmployeeRepository employees, LeaveRequestRepository leaves,
                           NotificationRepository notifications, AttendanceRepository attendances, PasswordEncoder encoder) {
        this.auth = auth; this.employees = employees; this.leaves = leaves;
        this.notifications = notifications; this.attendances = attendances; this.encoder = encoder;
    }

    @GetMapping("/home")
    public String home(HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        LocalDate today = LocalDate.now();
        List<LeaveRequest> approved = leaves.findByStatus(LeaveStatus.APPROVED);
        List<LeaveRequest> pendingAll = leaves.findByStatus(LeaveStatus.PENDING);
        long pending = me.isManager() ? pendingAll.size()
                : pendingAll.stream().filter(l -> l.getEmployee().getId().equals(me.getId())).count();
        long onLeave = approved.stream().filter(l -> !today.isBefore(l.getStartDate()) && !today.isAfter(l.getEndDate()))
                .map(l -> l.getEmployee().getId()).distinct().count();
        m.addAttribute("totalEmployees", employees.count());
        m.addAttribute("pending", pending);
        m.addAttribute("onLeave", onLeave);
        m.addAttribute("upcoming", approved.stream().filter(l -> !l.getEndDate().isBefore(today))
                .sorted(Comparator.comparing(LeaveRequest::getStartDate)).limit(5).toList());
        m.addAttribute("recent", notifications.findByRecipientOrderByCreatedAtDescIdDesc(me).stream().limit(4).toList());
        m.addAttribute("todayAtt", attendances.findByEmployeeAndWorkDate(me, today).orElse(null));
        return "home";
    }

    // ---------- Inbox ----------
    @GetMapping("/inbox")
    public String inbox(HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        m.addAttribute("items", notifications.findByRecipientOrderByCreatedAtDescIdDesc(me));
        return "inbox";
    }

    @GetMapping("/inbox/{id}/open")
    public String open(@PathVariable Long id, HttpSession s) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        String target = "/inbox";
        Notification n = notifications.findById(id).orElse(null);
        if (n != null && n.getRecipient().getId().equals(me.getId())) {
            n.setSeen(true);
            notifications.save(n);
            if (n.getLink() != null) target = n.getLink();
        }
        return "redirect:" + target;
    }

    @PostMapping("/inbox/read-all")
    public String readAll(HttpSession s) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        List<Notification> list = notifications.findByRecipientAndSeenFalse(me);
        list.forEach(n -> n.setSeen(true));
        notifications.saveAll(list);
        return "redirect:/inbox";
    }

    // ---------- Calendar ----------
    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) String ym, HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        YearMonth parsed;
        try { parsed = ym == null ? YearMonth.now() : YearMonth.parse(ym); } catch (Exception ex) { parsed = YearMonth.now(); }
        final YearMonth month = parsed;
        Map<Integer, List<String>> names = new LinkedHashMap<>();
        for (int d = 1; d <= month.lengthOfMonth(); d++) names.put(d, new ArrayList<>());
        for (LeaveRequest l : leaves.findByStatus(LeaveStatus.APPROVED)) {
            LocalDate from = l.getStartDate().isBefore(month.atDay(1)) ? month.atDay(1) : l.getStartDate();
            LocalDate to = l.getEndDate().isAfter(month.atEndOfMonth()) ? month.atEndOfMonth() : l.getEndDate();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) names.get(d.getDayOfMonth()).add(l.getEmployee().getName());
        }
        int blanks = month.atDay(1).getDayOfWeek().getValue() % 7;
        List<Integer> blankList = new ArrayList<>();
        for (int i = 0; i < blanks; i++) blankList.add(i);
        m.addAttribute("monthLabel", month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear());
        m.addAttribute("prev", month.minusMonths(1).toString());
        m.addAttribute("next", month.plusMonths(1).toString());
        m.addAttribute("blanks", blankList);
        m.addAttribute("names", names);
        m.addAttribute("todayDay", month.equals(YearMonth.now()) ? LocalDate.now().getDayOfMonth() : -1);
        return "calendar";
    }

    // ---------- Employees ----------
    @GetMapping("/employees")
    public String employeeList(@RequestParam(required = false) String q, HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        m.addAttribute("list", q == null || q.isBlank() ? employees.findAll(Sort.by("name")) : employees.search(q.trim()));
        m.addAttribute("q", q);
        return "employees";
    }

    // ---------- Settings ----------
    @GetMapping("/settings")
    public String settings(HttpSession s) {
        return auth.user(s) == null ? "redirect:/login" : "settings";
    }

    @PostMapping("/settings/profile")
    public String profile(@RequestParam String name, @RequestParam(required = false) String jobTitle,
                          @RequestParam(required = false) String department, HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        if (name.isBlank()) { ra.addFlashAttribute("error", "Name cannot be empty."); return "redirect:/settings"; }
        me.setName(name.trim());
        if (jobTitle != null && !jobTitle.isBlank()) me.setJobTitle(jobTitle.trim());
        if (department != null && !department.isBlank()) me.setDepartment(department.trim());
        employees.save(me);
        ra.addFlashAttribute("success", "Profile updated.");
        return "redirect:/settings";
    }

    @PostMapping("/settings/password")
    public String password(@RequestParam String current, @RequestParam String newPassword, @RequestParam String confirm,
                           HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        String err = null;
        if (!encoder.matches(current, me.getPassword())) err = "Current password is incorrect.";
        else if (newPassword.length() < 6) err = "New password must be at least 6 characters.";
        else if (!newPassword.equals(confirm)) err = "New passwords do not match.";
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/settings"; }
        me.setPassword(encoder.encode(newPassword));
        employees.save(me);
        ra.addFlashAttribute("success", "Password changed.");
        return "redirect:/settings";
    }
}
