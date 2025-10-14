package swing.ui;

import jdbc.domain.user.service.UserService;

import javax.swing.*;
import java.awt.*;

public class ContextBar extends JPanel {
    private final JLabel lblUser = new JLabel("로그인: 없음");
    private final JButton btnLogin = new JButton("로그인");
    private final JButton btnLogout = new JButton("로그아웃");
    private final UserService userService;

    public ContextBar(UserService userService) {
        this.userService = userService;
        setLayout(new FlowLayout(FlowLayout.LEFT));
    add(Box.createHorizontalStrut(32));
        add(lblUser);
        add(btnLogin);
        add(btnLogout);

        btnLogin.addActionListener(e -> openLogin());
        btnLogout.addActionListener(e -> doLogout());
    }

    private void openLogin() {
        LoginDialog dlg = new LoginDialog((Frame) SwingUtilities.getWindowAncestor(this), userService);
        dlg.setVisible(true);
        var u = Session.get().getCurrentUser();
        if (u != null) {
            lblUser.setText(String.format("%s(%s)", u.getName(), u.getRole()==null?"?":u.getRole().name()));
            // 로그인 사용자의 이름/역할만 표시
        } else {
            lblUser.setText("로그인: 없음");
        }
    }

    private void doLogout() {
        Session.get().logout();
        lblUser.setText("로그인: 없음");
    }

}
