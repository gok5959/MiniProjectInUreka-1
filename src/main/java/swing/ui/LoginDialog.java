package swing.ui;

import jdbc.domain.user.model.User;
import jdbc.domain.user.service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final UserService userService;
    private final JTextField tfUserId = new JTextField(8);

    public LoginDialog(Frame owner, UserService userService) {
        super(owner, "로그인 (데모)", true);
        this.userService = userService;
        setLayout(new FlowLayout());
        add(new JLabel("User ID:")); add(tfUserId);
        JButton btnLogin = new JButton("로그인"); JButton btnCancel = new JButton("취소");
        add(btnLogin); add(btnCancel);

        btnLogin.addActionListener(e -> doLogin());
        btnCancel.addActionListener(e -> setVisible(false));

        pack(); setLocationRelativeTo(owner);
    }

    private void doLogin() {
        Long id = null;
        try { id = Long.valueOf(tfUserId.getText().trim()); } catch (Exception e) { JOptionPane.showMessageDialog(this, "유효한 ID 입력", "경고", JOptionPane.WARNING_MESSAGE); return; }
        try {
            User u = userService.getById(id);
            if (u == null) { JOptionPane.showMessageDialog(this, "사용자 없음", "오류", JOptionPane.ERROR_MESSAGE); return; }
            Session.get().setCurrentUser(u);
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
