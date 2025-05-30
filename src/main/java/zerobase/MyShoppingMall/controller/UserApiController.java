//package zerobase.weather.controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import zerobase.weather.dto.user.AddUserRequestDTO;
//import zerobase.weather.service.UserService;
//
//@RequiredArgsConstructor
//@Controller
//public class UserApiController {
//    private final UserService userService;
//
//    @PostMapping("/user")
//    public String signup(AddUserRequestDTO request) {
//        userService.save(request);
//        return "redirect:/login";
//    }
//
//    @GetMapping("/logout")
//    public String logout(HttpServletRequest request, HttpServletResponse response){
//        new SecurityContextLogoutHandler().logout(request, response,
//                SecurityContextHolder.getContext().getAuthentication());
//        return "redirect:/login";
//    }
//}
