//package zerobase.weather.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//
//@RestController
//@RequiredArgsConstructor
//public class DatabaseCheckController {
//
//    private final DataSource dataSource;
//
//    @GetMapping("/check/db")
//    public String checkDatabaseConnection() {
//        try (Connection connection = dataSource.getConnection()) {
//            if (connection.isValid(2)) {
//                return " DB 연결 성공!";
//            } else {
//                return " DB 연결 실패!";
//            }
//        } catch (Exception e) {
//            return " DB 연결 중 예외 발생: " + e.getMessage();
//        }
//    }
//}
