package com.teamhub.leave.web;

import com.teamhub.leave.model.*;
import com.teamhub.leave.repo.*;
import com.teamhub.leave.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.util.*;

/** Attendance, Performance, Payroll and Recruitment pages. */
@Controller
public class HrController {
    private final Auth auth;
    private final EmployeeRepository employees;
    private final AttendanceRepository attendances;
    private final PerformanceReviewRepository reviews;
    private final PayrollRepository payrolls;
    private final JobOpeningRepository jobs;
    private final NotificationService notifier;

    public HrController(Auth auth, EmployeeRepository employees, AttendanceRepository attendances,
                        PerformanceReviewRepository reviews, PayrollRepository payrolls,
                        JobOpeningRepository jobs, NotificationService notifier) {
        this.auth = auth; this.employees = employees; this.attendances = attendances;
        this.reviews = reviews; this.payrolls = payrolls; this.jobs = jobs; this.notifier = notifier;
    }

    private String adminOnly(RedirectAttributes ra, String back) {
        ra.addFlashAttribute("error", "Only admins can do this.");
        return "redirect:" + back;
    }

    // ---------- Attendance ----------
    @GetMapping("/attendance")
    public String attendance(HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        m.addAttribute("todayAtt", attendances.findByEmployeeAndWorkDate(me, LocalDate.now()).orElse(null));
        m.addAttribute("records", me.isManager() ? attendances.findTop50ByOrderByWorkDateDescIdDesc()
                : attendances.findTop30ByEmployeeOrderByWorkDateDescIdDesc(me));
        return "attendance";
    }

    @PostMapping("/attendance/check-in")
    public String checkIn(HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        if (attendances.findByEmployeeAndWorkDate(me, LocalDate.now()).isPresent()) {
            ra.addFlashAttribute("error", "You have already checked in today.");
        } else {
            Attendance a = new Attendance();
            a.setEmployee(me);
            a.setWorkDate(LocalDate.now());
            a.setCheckIn(LocalTime.now().withSecond(0).withNano(0));
            attendances.save(a);
            ra.addFlashAttribute("success", "Checked in successfully.");
        }
        return "redirect:/attendance";
    }

    @PostMapping("/attendance/check-out")
    public String checkOut(HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        Attendance a = attendances.findByEmployeeAndWorkDate(me, LocalDate.now()).orElse(null);
        if (a == null) ra.addFlashAttribute("error", "You have not checked in today.");
        else if (a.getCheckOut() != null) ra.addFlashAttribute("error", "You have already checked out today.");
        else {
            a.setCheckOut(LocalTime.now().withSecond(0).withNano(0));
            attendances.save(a);
            ra.addFlashAttribute("success", "Checked out successfully.");
        }
        return "redirect:/attendance";
    }

    // ---------- Performance ----------
    @GetMapping("/performance")
    public String performance(HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        List<PerformanceReview> list = me.isManager() ? reviews.findAllByOrderByReviewDateDescIdDesc()
                : reviews.findByEmployeeOrderByReviewDateDescIdDesc(me);
        double avg = list.stream().mapToInt(PerformanceReview::getRating).average().orElse(0);
        m.addAttribute("list", list);
        m.addAttribute("avg", list.isEmpty() ? "-" : String.format("%.1f", avg));
        m.addAttribute("people", employees.findAll().stream().filter(e -> !e.getId().equals(me.getId())).toList());
        return "performance";
    }

    @PostMapping("/performance")
    public String addReview(@RequestParam Long employeeId, @RequestParam int rating, @RequestParam String comments,
                            HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        if (!me.isManager()) return adminOnly(ra, "/performance");
        Employee target = employees.findById(employeeId).orElse(null);
        if (target == null) ra.addFlashAttribute("error", "Select an employee.");
        else if (rating < 1 || rating > 5) ra.addFlashAttribute("error", "Rating must be between 1 and 5.");
        else if (comments.isBlank()) ra.addFlashAttribute("error", "Please add review comments.");
        else {
            PerformanceReview r = new PerformanceReview();
            r.setEmployee(target); r.setReviewer(me); r.setRating(rating); r.setComments(comments.trim());
            reviews.save(r);
            notifier.notify(target, "You received a performance review (" + rating + "/5) from " + me.getName() + ".", "/performance");
            ra.addFlashAttribute("success", "Review submitted.");
        }
        return "redirect:/performance";
    }

    // ---------- Payroll ----------
    @GetMapping("/payroll")
    public String payroll(HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        m.addAttribute("list", me.isManager() ? payrolls.findAllByOrderByPayMonthDescIdDesc()
                : payrolls.findByEmployeeOrderByPayMonthDescIdDesc(me));
        m.addAttribute("people", employees.findAll());
        m.addAttribute("thisMonth", YearMonth.now().toString());
        return "payroll";
    }

    @PostMapping("/payroll")
    public String addPayroll(@RequestParam Long employeeId, @RequestParam String payMonth, @RequestParam double basic,
                             @RequestParam double allowances, @RequestParam double deductions,
                             HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        if (!me.isManager()) return adminOnly(ra, "/payroll");
        Employee target = employees.findById(employeeId).orElse(null);
        if (target == null) ra.addFlashAttribute("error", "Select an employee.");
        else if (!payMonth.matches("\\d{4}-\\d{2}")) ra.addFlashAttribute("error", "Select a valid month.");
        else if (basic <= 0 || allowances < 0 || deductions < 0) ra.addFlashAttribute("error", "Basic must be positive; allowances and deductions cannot be negative.");
        else if (deductions > basic + allowances) ra.addFlashAttribute("error", "Deductions cannot exceed total earnings.");
        else if (payrolls.existsByEmployeeAndPayMonth(target, payMonth)) ra.addFlashAttribute("error", "A payslip for " + target.getName() + " already exists for " + payMonth + ".");
        else {
            Payroll p = new Payroll();
            p.setEmployee(target); p.setPayMonth(payMonth); p.setBasic(basic); p.setAllowances(allowances); p.setDeductions(deductions);
            payrolls.save(p);
            notifier.notify(target, "Your payslip for " + payMonth + " is ready.", "/payroll");
            ra.addFlashAttribute("success", "Payslip generated.");
        }
        return "redirect:/payroll";
    }

    // ---------- Recruitment ----------
    @GetMapping("/recruitment")
    public String recruitment(HttpSession s, Model m) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        m.addAttribute("list", me.isManager() ? jobs.findAllByOrderByIdDesc() : jobs.findByStatusOrderByIdDesc("OPEN"));
        return "recruitment";
    }

    @PostMapping("/recruitment")
    public String addJob(@RequestParam String title, @RequestParam String department, @RequestParam String location,
                         @RequestParam int openings, HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        if (!me.isManager()) return adminOnly(ra, "/recruitment");
        if (title.isBlank() || department.isBlank() || location.isBlank()) ra.addFlashAttribute("error", "Title, department and location are required.");
        else if (openings < 1) ra.addFlashAttribute("error", "Openings must be at least 1.");
        else {
            JobOpening j = new JobOpening();
            j.setTitle(title.trim()); j.setDepartment(department.trim()); j.setLocation(location.trim()); j.setOpenings(openings);
            jobs.save(j);
            ra.addFlashAttribute("success", "Job opening posted.");
        }
        return "redirect:/recruitment";
    }

    @PostMapping("/recruitment/{id}/toggle")
    public String toggle(@PathVariable Long id, HttpSession s, RedirectAttributes ra) {
        Employee me = auth.user(s);
        if (me == null) return "redirect:/login";
        if (!me.isManager()) return adminOnly(ra, "/recruitment");
        jobs.findById(id).ifPresent(j -> { j.setStatus(j.isOpen() ? "CLOSED" : "OPEN"); jobs.save(j); });
        return "redirect:/recruitment";
    }
}
