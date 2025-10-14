package swing.ui.panels;

import jdbc.domain.favorite.dto.FavoriteDto;
import jdbc.domain.favorite.service.FavoriteService;
import swing.ui.Session;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritesPanel extends JPanel {
    private final FavoriteService favoriteService = new FavoriteService();
    private final FavoritesTableModel tableModel = new FavoritesTableModel();
    private final JTable table = new JTable(tableModel);
    private int limit = 20, offset = 0;

    public FavoritesPanel() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnReload = new JButton("새로고침"); top.add(btnReload);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrev = new JButton("이전"); JButton btnNext = new JButton("다음"); JButton btnRemove = new JButton("찜 취소");
        bottom.add(btnPrev); bottom.add(btnNext); bottom.add(Box.createHorizontalStrut(16)); bottom.add(btnRemove);
        add(bottom, BorderLayout.SOUTH);

        btnReload.addActionListener(e -> { offset = 0; load(); });
        btnPrev.addActionListener(e -> { offset = Math.max(0, offset - limit); load(); });
        btnNext.addActionListener(e -> { offset = offset + limit; load(); });

        btnRemove.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "항목을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE); return; }
            var dto = tableModel.getAt(r);
            Long userId = Session.get().getUserId();
            if (userId == null || !userId.equals(dto.userId())) { JOptionPane.showMessageDialog(this, "본인의 찜만 취소할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; }
            try {
                boolean newActive = favoriteService.toggleFavorite(userId, dto.productId());
                JOptionPane.showMessageDialog(this, newActive ? "찜 추가 완료" : "찜이 취소되었습니다.", "정보", JOptionPane.INFORMATION_MESSAGE);
                load();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        });

        // session change -> reload
        Session.get().addListener(new Session.Listener() {
            @Override public void onLogin(jdbc.domain.user.model.User user) { load(); }
            @Override public void onLogout() { load(); }
        });

        load();
    }

    private void load() {
        Long userId = Session.get().getUserId();
        if (userId == null) { tableModel.setData(List.of()); return; }
        try {
            java.util.List<FavoriteDto> rows = favoriteService.findByUser(userId, limit, offset);
            tableModel.setData(rows);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class FavoritesTableModel extends AbstractTableModel {
        private final String[] cols = {"상품ID", "이름", "가격", "찜시간"};
        private List<FavoriteDto> rows = new ArrayList<>();

        public void setData(List<FavoriteDto> data) { this.rows = data; fireTableDataChanged(); }
        public FavoriteDto getAt(int r) { return rows.get(r); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) { FavoriteDto f = rows.get(r); return switch (c) { case 0 -> f.productId(); case 1 -> f.productName() == null ? "?" : f.productName(); case 2 -> f.productPrice() == null ? "?" : f.productPrice(); case 3 -> f.createdAt(); default -> ""; }; }
    }
}
