package swing.ui.panels;

import jdbc.common.paging.Page;
import jdbc.domain.order.dto.OrderSummary;
import jdbc.domain.order.model.OrderState;
import jdbc.domain.order.service.OrderService;
import jdbc.domain.review.service.ReviewService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import swing.ui.Session;

public class BuyerOrdersPanel extends JPanel {
    private final OrderService orderService;
    // buyer id is read from Session when needed
    private final ReviewService reviewService;

    private final OrdersTableModel tableModel = new OrdersTableModel();
    private final JTable table = new JTable(tableModel);
    private int limit = 10, offset = 0;
    private final JComboBox<OrderState> cbState = new JComboBox<>(OrderState.values());

    public BuyerOrdersPanel(OrderService orderService, ReviewService reviewService) {
        this.orderService = orderService;
        this.reviewService = reviewService;

        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("상태 필터:"));
        cbState.insertItemAt(null,0); cbState.setSelectedIndex(0);
        top.add(cbState);
        JButton btnReload = new JButton("새로고침"); top.add(btnReload);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnPrev = new JButton("이전"); JButton btnNext = new JButton("다음");
    JButton btnCancel = new JButton("취소 (요청 상태만)");
    JButton btnReview = new JButton("리뷰 작성");
    bottom.add(btnPrev); bottom.add(btnNext); bottom.add(Box.createHorizontalStrut(16)); bottom.add(btnCancel); bottom.add(btnReview);
        add(bottom, BorderLayout.SOUTH);

        btnReload.addActionListener(e -> { offset=0; load(); });
        btnPrev.addActionListener(e -> { offset = Math.max(0, offset - limit); load(); });
        btnNext.addActionListener(e -> { offset = offset + limit; load(); });

        btnCancel.addActionListener(e -> withSelectedOrderId(oid -> {
            Long buyerId = Session.get().getUserId(); if (buyerId==null) { JOptionPane.showMessageDialog(this, "로그인 후 취소할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; }
            SwingWorker<Void,Void> w = new SwingWorker<>(){ Exception err; @Override protected Void doInBackground(){ try { orderService.cancelByBuyer(oid, buyerId); } catch(Exception ex){ err=ex; } return null; } @Override protected void done(){ if (err!=null) JOptionPane.showMessageDialog(BuyerOrdersPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); load(); } }; w.execute();
        }));

        btnReview.addActionListener(e -> withSelectedOrderId(oid -> {
            Long buyerId = Session.get().getUserId(); if (buyerId==null) { JOptionPane.showMessageDialog(this, "로그인 후 리뷰를 작성할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; }
            // Show review dialog
            ReviewDialog dlg = new ReviewDialog(SwingUtilities.getWindowAncestor(BuyerOrdersPanel.this), oid, buyerId, reviewService);
            dlg.setVisible(true);
            load();
        }));

        // 세션 변경시 단순히 리로드
        Session.Listener listener = new Session.Listener() {
            @Override public void onLogin(jdbc.domain.user.model.User user) { load(); }
            @Override public void onLogout() { load(); }
        };
        Session.get().addListener(listener);

        load();
    }

    private void withSelectedOrderId(LongConsumer fn){ int row = table.getSelectedRow(); if (row<0){ JOptionPane.showMessageDialog(this, "항목을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE); return; } Long id = tableModel.getIdAt(row); fn.accept(id); }

    private void load(){ Long buyerId = Session.get().getUserId(); if (buyerId==null){ tableModel.setData(List.of()); return; } SwingWorker<Void,Void> w = new SwingWorker<>(){ Page<OrderSummary> page; Exception err; @Override protected Void doInBackground(){ try{ OrderState st = (OrderState) cbState.getSelectedItem(); page = orderService.getBuyerOrders(buyerId, limit, offset, st); } catch(Exception ex){ err=ex; } return null; } @Override protected void done(){ if (err!=null){ JOptionPane.showMessageDialog(BuyerOrdersPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); return; } tableModel.setData(page==null?List.of():page.content()); } }; w.execute(); }

    static class OrdersTableModel extends AbstractTableModel { private final String[] cols = {"주문ID","상품ID","상품","판매자","구매자","가격","상태","리뷰여부","생성일"}; private List<OrderSummary> rows = new ArrayList<>(); public void setData(List<OrderSummary> data){ this.rows = data; fireTableDataChanged(); } public Long getIdAt(int r){ return rows.get(r).orderId(); } @Override public int getRowCount(){ return rows.size(); } @Override public int getColumnCount(){ return cols.length; } @Override public String getColumnName(int c){ return cols[c]; } @Override public Object getValueAt(int r,int c){ OrderSummary o = rows.get(r); return switch(c){ case 0 -> o.orderId(); case 1 -> o.productId(); case 2 -> o.productName(); case 3 -> o.sellerName(); case 4 -> o.buyerName(); case 5 -> o.priceAtOrder(); case 6 -> o.orderState(); case 7 -> o.reviewStatus(); case 8 -> o.createdAt(); default -> ""; }; } }

    private static Long readLong(String s){ try { return Long.valueOf(s.trim()); } catch(Exception e){ return null; } }
    @FunctionalInterface interface LongConsumer { void accept(Long id); }
}
