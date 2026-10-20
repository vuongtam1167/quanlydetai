package com.example.quanlydetai.config;

import com.example.quanlydetai.dto.CurrentUser;
import com.example.quanlydetai.exception.WebExceptionHandler;
import com.example.quanlydetai.model.Lecturer;
import com.example.quanlydetai.model.Student;
import com.example.quanlydetai.model.enums.Role;
import com.example.quanlydetai.repository.LecturerRepository;
import com.example.quanlydetai.repository.StudentRepository;
import com.example.quanlydetai.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

/**
 * Bơm dữ liệu dùng chung cho layout (sidebar, header) vào MỌI trang HTML:
 * me (người dùng), menu (theo vai trò), currentPath (để tô sáng mục đang chọn).
 * Nhờ vậy từng controller không cần tự thêm các thông tin này.
 */
@Component
public class LayoutModelInterceptor implements HandlerInterceptor {

    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final MenuService menuService;

    public LayoutModelInterceptor(StudentRepository studentRepository,
                                  LecturerRepository lecturerRepository,
                                  MenuService menuService) {
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.menuService = menuService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView mav) {
        if (mav == null || !mav.hasView()) {
            return;
        }
        String viewName = mav.getViewName();
        if (viewName == null || viewName.startsWith("redirect:") || viewName.startsWith("forward:")) {
            return;
        }

        Object appMessage = request.getAttribute(WebExceptionHandler.ERROR_MESSAGE_ATTR);
        if (appMessage != null) {
            mav.addObject("errorMessage", appMessage);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return;
        }
        CurrentUser me = resolveCurrentUser(auth);
        mav.addObject("me", me);
        mav.addObject("menu", menuService.menuFor(me.role()));

        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        mav.addObject("currentPath", path);
    }

    private CurrentUser resolveCurrentUser(Authentication auth) {
        String username = auth.getName();
        Role role = null;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if (a != null && a.startsWith("ROLE_")) {
                try {
                    role = Role.valueOf(a.substring(5));
                    break;
                } catch (IllegalArgumentException ignored) {
                    // bỏ qua authority không thuộc enum Role
                }
            }
        }

        String name = username;
        String code = username;
        if (role == Role.SINH_VIEN) {
            Optional<Student> s = studentRepository.findByUserUsername(username);
            if (s.isPresent()) {
                name = s.get().getFullName();
                code = s.get().getStudentCode();
            }
        } else if (role == Role.GIANG_VIEN || role == Role.TRUONG_BO_MON) {
            Optional<Lecturer> l = lecturerRepository.findByUserUsername(username);
            if (l.isPresent()) {
                name = l.get().getFullName();
                code = l.get().getLecturerCode();
            }
        }
        return new CurrentUser(username, name, code, role, menuService.roleLabel(role), initialOf(name));
    }

    private String initialOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String last = parts[parts.length - 1];
        return last.substring(0, 1).toUpperCase();
    }
}
