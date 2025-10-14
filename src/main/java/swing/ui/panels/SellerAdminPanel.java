package swing.ui.panels;

import jdbc.common.paging.Page;
import jdbc.domain.order.dto.OrderSummary;
import jdbc.domain.order.model.OrderState;
import jdbc.domain.order.service.OrderService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import swing.ui.Session;

public class SellerAdminPanel extends JPanel {
    private final OrderService orderService;
    // seller id is read from Session when needed

    private final OrdersTableModel tableModel = new OrdersTableModel();
    private final JTable table = new JTable(tableModel);
    private int limit = 10, offset = 0;
    private final JComboBox<String> cbMode = new JComboBox<>(new String[]{"판매자","관리자"});
    private final JComboBox<OrderState> cbState = new JComboBox<>(OrderState.values());
    private final JTextField tfBuyerId = new JTextField(6);
    private final JTextField tfProductId = new JTextField(6);

    public SellerAdminPanel(OrderService orderService) {
        this.orderService = orderService;

        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("모드:")); top.add(cbMode);
        top.add(new JLabel("상태:")); cbState.insertItemAt(null,0); cbState.setSelectedIndex(0); top.add(cbState);
        top.add(new JLabel("구매자 ID:")); top.add(tfBuyerId);
        top.add(new JLabel("상품 ID:")); top.add(tfProductId);
        JButton btnReload = new JButton("새로고침"); top.add(btnReload);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrev = new JButton("이전"); JButton btnNext = new JButton("다음");
        JButton btnAccept = new JButton("수락"); JButton btnReject = new JButton("거절"); JButton btnComplete = new JButton("완료");
        bottom.add(btnPrev); bottom.add(btnNext); bottom.add(Box.createHorizontalStrut(16)); bottom.add(btnAccept); bottom.add(btnReject); bottom.add(btnComplete);
        add(bottom, BorderLayout.SOUTH);

        btnReload.addActionListener(e -> { offset=0; load(); });
        btnPrev.addActionListener(e -> { offset=Math.max(0, offset-limit); load(); });
        btnNext.addActionListener(e -> { offset=offset+limit; load(); });

    btnAccept.addActionListener(e -> withSelectedOrderId(oid -> { Long sellerId = Session.get().getUserId(); if (sellerId==null){ JOptionPane.showMessageDialog(this, "로그인 후 수락할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; } runAction(() -> orderService.accept(oid, sellerId)); }));
    btnReject.addActionListener(e -> withSelectedOrderId(oid -> { Long sellerId = Session.get().getUserId(); if (sellerId==null){ JOptionPane.showMessageDialog(this, "로그인 후 거절할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; } runAction(() -> orderService.reject(oid, sellerId)); }));
    btnComplete.addActionListener(e -> withSelectedOrderId(oid -> { Long sellerId = Session.get().getUserId(); if (sellerId==null){ JOptionPane.showMessageDialog(this, "로그인 후 완료 처리할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; } runAction(() -> orderService.complete(oid, sellerId)); }));

        cbMode.addActionListener(e -> { boolean isSeller = "판매자".equals(cbMode.getSelectedItem()); btnAccept.setEnabled(isSeller); btnReject.setEnabled(isSeller); btnComplete.setEnabled(isSeller); offset=0; load(); });
        cbMode.setSelectedIndex(0);
        boolean isSeller = "판매자".equals(cbMode.getSelectedItem()); btnAccept.setEnabled(isSeller); btnReject.setEnabled(isSeller); btnComplete.setEnabled(isSeller);

        // 세션 변경시 리로드
        Session.Listener listener = new Session.Listener() {
            @Override public void onLogin(jdbc.domain.user.model.User user) { load(); }
            @Override public void onLogout() { load(); }
        };
        Session.get().addListener(listener);
        // 패널 생성 시 이미 로그인되어 있으면 즉시 로드
        if (Session.get().getCurrentUser() != null) { load(); }
        // 패널이 제거될 때 리스너 자동 제거(메모리 누수 방지)
        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override public void ancestorAdded(javax.swing.event.AncestorEvent event) {}
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent event) { Session.get().removeListener(listener); }
        });

        load();
    }

    private void runAction(Runnable r){ SwingWorker<Void,Void> w = new SwingWorker<>(){ Exception err; @Override protected Void doInBackground(){ try { r.run(); } catch(Exception ex){ err=ex; } return null; } @Override protected void done(){ if (err!=null) {
                String msg = err.getClass().getSimpleName() + ": " + (err.getMessage()==null?"오류 발생":err.getMessage());
                JOptionPane.showMessageDialog(SellerAdminPanel.this, msg, "오류", JOptionPane.ERROR_MESSAGE);
            }
            load();
        } };
        w.execute();
    }

    private void withSelectedOrderId(LongConsumer fn){ int row = table.getSelectedRow(); if (row<0){ JOptionPane.showMessageDialog(this, "항목을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE); return; } Long id = tableModel.getIdAt(row); fn.accept(id); }

    private void load(){ SwingWorker<Void,Void> w = new SwingWorker<>(){ Page<OrderSummary> page; Exception err; @Override protected Void doInBackground(){ try{ OrderState st = (OrderState) cbState.getSelectedItem(); if ("판매자".equals(cbMode.getSelectedItem())){ Long sellerId = Session.get().getUserId(); if (sellerId==null) return null; page = orderService.getSellerOrders(sellerId, limit, offset, st); } else { Long b = readLong(tfBuyerId.getText()); Long p = readLong(tfProductId.getText()); page = orderService.getAdminOrders(limit, offset, st, b, p); } } catch(Exception ex){ err=ex; } return null; } @Override protected void done(){ if (err!=null){ JOptionPane.showMessageDialog(SellerAdminPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); return; } tableModel.setData(page==null?List.of():page.content()); } }; w.execute(); }

    static class OrdersTableModel extends AbstractTableModel { private final String[] cols = {"주문ID","상품ID","상품","판매자","구매자","가격","상태","리뷰여부","생성일"}; private List<OrderSummary> rows = new ArrayList<>(); public void setData(List<OrderSummary> data){ this.rows = data; fireTableDataChanged(); } public Long getIdAt(int r){ return rows.get(r).orderId(); } @Override public int getRowCount(){ return rows.size(); } @Override public int getColumnCount(){ return cols.length; } @Override public String getColumnName(int c){ return cols[c]; } @Override public Object getValueAt(int r,int c){ OrderSummary o = rows.get(r); return switch(c){ case 0 -> o.orderId(); case 1 -> o.productId(); case 2 -> o.productName(); case 3 -> o.sellerName(); case 4 -> o.buyerName(); case 5 -> o.priceAtOrder(); case 6 -> o.orderState(); case 7 -> o.reviewStatus(); case 8 -> o.createdAt(); default -> ""; }; } }

    private static Long readLong(String s){ try { return Long.valueOf(s.trim()); } catch(Exception e){ return null; } }
    @FunctionalInterface interface LongConsumer { void accept(Long id); }
}
