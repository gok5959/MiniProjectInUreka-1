package swing.ui.panels;

import jdbc.domain.review.service.ReviewService;

import javax.swing.*;
import java.awt.*;

public class ReviewDialog extends JDialog {
    public ReviewDialog(Window owner, Long orderId, Long reviewerId, ReviewService reviewService) {
        super(owner, "리뷰 작성", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0,1));
    SpinnerNumberModel ratingModel = new SpinnerNumberModel(5.0, 0.5, 5.0, 0.5);
    JSpinner spRating = new JSpinner(ratingModel);
    JTextField tfTitle = new JTextField(40);
    JTextArea taContent = new JTextArea(6,40);
    form.add(new JLabel("제목:")); form.add(tfTitle);
    form.add(new JLabel("평점 (0.5 단위, 0.5~5.0):")); form.add(spRating);
    form.add(new JLabel("내용:")); form.add(new JScrollPane(taContent));
        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("작성"); JButton btnCancel = new JButton("취소");
        bottom.add(btnCancel); bottom.add(btnOk);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> {
            Double ratingD = ((Number) spRating.getValue()).doubleValue();
            java.math.BigDecimal rating = java.math.BigDecimal.valueOf(ratingD).setScale(1, java.math.RoundingMode.HALF_UP);
            String content = taContent.getText().trim();
            String title = tfTitle.getText().trim();
            SwingWorker<Void,Void> w = new SwingWorker<>(){ Exception err; @Override protected Void doInBackground(){ try{ reviewService.createReview(orderId, reviewerId, title, content, rating); } catch(Exception ex){ err = ex; } return null;} @Override protected void done(){ if (err!=null) { JOptionPane.showMessageDialog(ReviewDialog.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); } else { JOptionPane.showMessageDialog(ReviewDialog.this, "리뷰가 등록되었습니다.", "정보", JOptionPane.INFORMATION_MESSAGE); dispose(); } } }; w.execute();
        });

        pack(); setLocationRelativeTo(owner);
    }
}
