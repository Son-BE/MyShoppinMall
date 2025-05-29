package zerobase.MyShoppingMall.Admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminProperties {
    @Value("${admin.email}")
    private String email;

    @Value("${admin.password}")
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
