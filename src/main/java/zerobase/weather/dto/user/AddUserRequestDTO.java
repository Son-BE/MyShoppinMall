package zerobase.weather.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddUserRequestDTO {
    private String email;
    private String password;

    private String name;
    private String phone;
    private LocalDateTime registeredAt;
}
