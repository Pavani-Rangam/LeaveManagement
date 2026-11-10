package com.teamhub.leave.web;

import com.teamhub.leave.model.*;
import com.teamhub.leave.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
public class LeaveController {
    private final Auth auth;
    private final LeaveService service;

    public LeaveController(Auth auth, LeaveService service) {
        this.auth = auth;
        this.service = service;
    }

    @GetMapping("/leave/apply")
    public String form(HttpSession session, Model m) {
        Employee me = auth.user(session);
        if (me == null) return "redirect:/login";
        if (me.isManager()) return "redirect:/dashboard";
        prepare(m, me);
        return "apply";
    }

    @PostMapping("/leave/apply")
    public String apply(@RequestParam(required = false) LeaveType type,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) String reason,
                        HttpSession session, Model m, RedirectAttributes ra) {
        Employee me = auth.user(session);
        if (me == null) return "redirect:/login";
        if (me.isManager()) {
            ra.addFlashAttribute("error", "Admins approve or reject leave requests and cannot apply for leave.");
            return "redirect:/dashboard";
        }
        try {
            service.apply(me, type, startDate, endDate, reason);
            ra.addFlashAttribute("success", "Leave request submitted successfully. It is now pending approval.");
            return "redirect:/dashboard";
        } catch (LeaveException ex) {
            prepare(m, me);
            m.addAttribute("error", ex.getMessage());
            m.addAttribute("fType", type);
            m.addAttribute("fStart", startDate);
            m.addAttribute("fEnd", endDate);
            m.addAttribute("fReason", reason);
            return "apply";
        }
    }

    @PostMapping("/leave/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        return decide(id, session, ra, true);
    }

    @PostMapping("/leave/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        return decide(id, session, ra, false);
    }

    private String decide(Long id, HttpSession session, RedirectAttributes ra, boolean approve) {
        Employee me = auth.user(session);
        if (me == null) return "redirect:/login";
        try {
            if (approve) service.approve(id, me); else service.reject(id, me);
            ra.addFlashAttribute("success", "Leave request " + (approve ? "approved." : "rejected."));
        } catch (LeaveException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    private void prepare(Model m, Employee me) {
        m.addAttribute("me", me);
        m.addAttribute("manager", me.isManager());
        m.addAttribute("types", LeaveType.values());
        m.addAttribute("today", LocalDate.now());
    }
}
