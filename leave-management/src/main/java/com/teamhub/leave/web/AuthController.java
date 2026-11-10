package com.teamhub.leave.web;

import com.teamhub.leave.model.Employee;
import com.teamhub.leave.repo.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final EmployeeRepository employees;
    private final PasswordEncoder encoder;
    @Value("${app.admin-key}")
    private String adminKey;

    public AuthController(EmployeeRepository employees, PasswordEncoder encoder) {
        this.employees = employees;
        this.encoder = encoder;
    }

    @GetMapping("/")
    public String home() { return "redirect:/home"; }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Employee e = employees.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (e == null || !encoder.matches(password, e.getPassword())) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
        session.setAttribute("empId", e.getId());
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                           @RequestParam String confirm, @RequestParam String role,
                           @RequestParam(required = false) String department, @RequestParam(required = false) String jobTitle,
                           @RequestParam(required = false) String key, Model m, RedirectAttributes ra) {
        m.addAttribute("rName", name); m.addAttribute("rEmail", email); m.addAttribute("rRole", role);
        m.addAttribute("rDept", department); m.addAttribute("rTitle", jobTitle);
        String mail = email.trim().toLowerCase();
        String err = null;
        if (name.isBlank()) err = "Full name is required.";
        else if (!mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) err = "Enter a valid email address.";
        else if (password.length() < 6) err = "Password must be at least 6 characters.";
        else if (!password.equals(confirm)) err = "Passwords do not match.";
        else if (!role.equals("EMPLOYEE") && !role.equals("MANAGER")) err = "Select a valid role.";
        else if (role.equals("MANAGER") && !adminKey.equals(key)) err = "Invalid admin registration key.";
        else if (employees.existsByEmail(mail)) err = "An account with this email already exists.";
        if (err != null) { m.addAttribute("error", err); return "register"; }

        Employee e = new Employee();
        e.setName(name.trim()); e.setEmail(mail); e.setRole(role);
        e.setPassword(encoder.encode(password));
        e.setDepartment(department == null || department.isBlank() ? "General" : department.trim());
        e.setJobTitle(jobTitle == null || jobTitle.isBlank() ? (role.equals("MANAGER") ? "Admin" : "Employee") : jobTitle.trim());
        e = employees.save(e);
        e.setEmpCode(String.format("EMP-%04d", e.getId()));
        employees.save(e);
        ra.addFlashAttribute("success", "Registration successful. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
