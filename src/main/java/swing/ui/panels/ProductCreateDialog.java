package swing.ui.panels;

import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;
import jdbc.domain.product.service.ProductService;
import swing.ui.Session;

import javax.swing.*;
import java.awt.*;

public class ProductCreateDialog extends JDialog {
    private final JTextField tfName = new JTextField(20);
    private final JTextField tfPrice = new JTextField(10);
    private final JTextField tfCategory = new JTextField(6);
    private final JComboBox<ProductStatus> cbStatus = new JComboBox<>(ProductStatus.values());
    private final ProductService productService = new ProductService();

    public ProductCreateDialog(Window owner) {
        super(owner, "상품 등록", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(new JLabel("이름:")); form.add(tfName);
        form.add(new JLabel("가격:")); form.add(tfPrice);
        form.add(new JLabel("카테고리 ID:")); form.add(tfCategory);
        form.add(new JLabel("상태:")); form.add(cbStatus);
        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCreate = new JButton("등록"); JButton btnCancel = new JButton("취소");
        bottom.add(btnCancel); bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> setVisible(false));
        btnCreate.addActionListener(e -> doCreate());

        pack(); setLocationRelativeTo(owner);
    }

    private void doCreate() {
        String name = tfName.getText().trim();
        Long price;
        Long categoryId = null;
        try { price = Long.valueOf(tfPrice.getText().trim()); } catch(Exception e) { JOptionPane.showMessageDialog(this, "유효한 가격을 입력하세요", "경고", JOptionPane.WARNING_MESSAGE); return; }
        if (!tfCategory.getText().trim().isEmpty()) {
            try { categoryId = Long.valueOf(tfCategory.getText().trim()); } catch(Exception e) { JOptionPane.showMessageDialog(this, "유효한 카테고리 ID", "경고", JOptionPane.WARNING_MESSAGE); return; }
        }
        ProductStatus status = (ProductStatus) cbStatus.getSelectedItem();
        Long sellerId = Session.get().getUserId();
        if (sellerId == null) { JOptionPane.showMessageDialog(this, "로그인 후 등록하세요", "경고", JOptionPane.WARNING_MESSAGE); return; }

        Product p = new Product(name, price, categoryId, sellerId, status);
        try {
            productService.create(p);
            JOptionPane.showMessageDialog(this, "상품 등록 완료", "정보", JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
