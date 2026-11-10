package com.teamhub.leave.web;

import com.teamhub.leave.model.*;
import com.teamhub.leave.repo.LeaveRequestRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.IntStream;

@Controller
public class DashboardController {
    private final Auth auth;
    private final LeaveRequestRepository repo;

    public DashboardController(Auth auth, LeaveRequestRepository repo) {
        this.auth = auth;
        this.repo = repo;
    }

    public record TypeStat(String label, long count, int pct, String color) {}

    private static boolean covers(LeaveRequest l, LocalDate d) {
        return !d.isBefore(l.getStartDate()) && !d.isAfter(l.getEndDate());
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model m) {
        Employee me = auth.user(session);
        if (me == null) return "redirect:/login";

        // Managers see everyone's requests, employees only their own
        List<LeaveRequest> all = me.isManager()
                ? repo.findAllByOrderBySubmitDateDescIdDesc()
                : repo.findByEmployeeOrderBySubmitDateDescIdDesc(me);
        List<LeaveRequest> approved = all.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).toList();
        LocalDate today = LocalDate.now();
        int total = approved.size();

        // Leave type breakdown + donut gradient
        LeaveType[] order = {LeaveType.ANNUAL, LeaveType.SICK, LeaveType.OTHER, LeaveType.CASUAL};
        String[] colors = {"#0b4f3c", "#2fbf8f", "#bfe9d6", "#d9d9d9"};
        List<TypeStat> stats = new ArrayList<>();
        StringBuilder g = new StringBuilder("conic-gradient(");
        double acc = 0;
        for (int i = 0; i < order.length; i++) {
            final LeaveType t = order[i];
            long c = approved.stream().filter(l -> l.getType() == t).count();
            int pct = total == 0 ? 0 : (int) Math.round(c * 100.0 / total);
            stats.add(new TypeStat(t.getLabel(), c, pct, colors[i]));
            if (c > 0) {
                double start = acc;
                acc += c * 100.0 / total;
                g.append(colors[i]).append(' ').append(start).append("% ").append(acc).append("%,");
            }
        }
        String donut = total == 0 ? "#e5e5e5" : g.substring(0, g.length() - 1) + ")";

        // Calendar for the current month (week starts Sunday)
        YearMonth ym = YearMonth.now();
        Set<Integer> leaveDays = new HashSet<>();
        for (LeaveRequest l : approved)
            for (LocalDate d = l.getStartDate(); !d.isAfter(l.getEndDate()); d = d.plusDays(1))
                if (YearMonth.from(d).equals(ym)) leaveDays.add(d.getDayOfMonth());
        int blanks = ym.atDay(1).getDayOfWeek().getValue() % 7;

        // Weekly overview (Mon-Fri)
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        List<String> weekLabels = List.of("Mon", "Tue", "Wed", "Thu", "Fri");
        List<Integer> weekCounts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LocalDate d = monday.plusDays(i);
            weekCounts.add((int) approved.stream().filter(l -> covers(l, d)).count());
        }
        int max = Collections.max(weekCounts);
        List<Integer> weekHeights = weekCounts.stream().map(c -> max == 0 ? 4 : Math.max(6, c * 100 / max)).toList();

        m.addAttribute("me", me);
        m.addAttribute("manager", me.isManager());
        m.addAttribute("requests", all);
        m.addAttribute("recent", all.stream().limit(3).toList());
        m.addAttribute("stats", stats);
        m.addAttribute("total", total);
        m.addAttribute("onLeave", approved.stream().filter(l -> covers(l, today)).count());
        m.addAttribute("donut", donut);
        m.addAttribute("monthLabel", ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear());
        m.addAttribute("blanks", IntStream.range(0, blanks).boxed().toList());
        m.addAttribute("days", IntStream.rangeClosed(1, ym.lengthOfMonth()).boxed().toList());
        m.addAttribute("leaveDays", leaveDays);
        m.addAttribute("todayDay", today.getDayOfMonth());
        m.addAttribute("weekLabels", weekLabels);
        m.addAttribute("weekCounts", weekCounts);
        m.addAttribute("weekHeights", weekHeights);
        return "dashboard";
    }
}
