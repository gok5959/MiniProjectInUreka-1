package swing.ui.panels;

import jdbc.common.paging.Page;
import jdbc.domain.product.model.ProductStatus;
import jdbc.domain.product.model.ProductWithMetrics;
import jdbc.domain.product.service.ProductService;
import jdbc.domain.order.service.OrderService;
import jdbc.domain.review.service.ReviewService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import swing.ui.Session;

public class ProductsPanel extends JPanel {
    private final ProductService productService;
    private final OrderService orderService;
    // session-based user id access (no text fields)

    private final ProductsTableModel tableModel = new ProductsTableModel();
    private final JTable table = new JTable(tableModel);
    private java.util.Set<Long> favoriteSet = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    private int limit = 10;
    private int offset = 0;
    private final JTextField tfSearch = new JTextField(16);
    private final ReviewService reviewService;
    private final jdbc.domain.favorite.service.FavoriteService favoriteService = new jdbc.domain.favorite.service.FavoriteService();
    private final JButton btnFavorite = new JButton("찜/취소");

    public ProductsPanel(ProductService productService, OrderService orderService, ReviewService reviewService) {
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;

        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("검색:"));
        top.add(tfSearch);
    JButton btnSearch = new JButton("검색");
    JButton btnReset = new JButton("초기화");
    JButton btnCreate = new JButton("등록");
        top.add(btnSearch);
    top.add(btnReset);
    top.add(Box.createHorizontalStrut(8));
    top.add(btnCreate);
        top.add(new JLabel("  페이지크기:"));
        JComboBox<Integer> cbLimit = new JComboBox<>(new Integer[]{5, 10, 20, 50});
        cbLimit.setSelectedItem(limit);
        top.add(cbLimit);
        add(top, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrev = new JButton("이전");
        JButton btnNext = new JButton("다음");
        JButton btnDetail = new JButton("상세보기 (+조회)");
        JButton btnLike = new JButton("좋아요");
        JButton btnUnlike = new JButton("좋아요 취소");
    JButton btnReserve = new JButton("예약 요청 (구매자)");
    JButton btnShowReviews = new JButton("리뷰 보기");
        JButton btnDelete = new JButton("삭제 (판매자)");
        bottom.add(btnPrev);
        bottom.add(btnNext);
        bottom.add(Box.createHorizontalStrut(16));
        bottom.add(btnDetail);
        bottom.add(btnLike);
        bottom.add(btnUnlike);
        bottom.add(Box.createHorizontalStrut(16));
        bottom.add(btnDelete);
        bottom.add(Box.createHorizontalStrut(16));
    bottom.add(btnReserve);
    bottom.add(Box.createHorizontalStrut(8));
    bottom.add(btnShowReviews);
    bottom.add(Box.createHorizontalStrut(8));
    bottom.add(btnFavorite);
        add(bottom, BorderLayout.SOUTH);

        // actions
        cbLimit.addActionListener(e -> {
            limit = (int) cbLimit.getSelectedItem();
            offset = 0;
            load();
        });
        btnSearch.addActionListener(e -> {
            offset = 0;
            load();
        });
        btnReset.addActionListener(e -> {
            tfSearch.setText("");
            offset = 0;
            load();
        });
        btnCreate.addActionListener(e -> {
            ProductCreateDialog dlg = new ProductCreateDialog(SwingUtilities.getWindowAncestor(ProductsPanel.this));
            dlg.setVisible(true);
            load();
        });
        btnPrev.addActionListener(e -> {
            offset = Math.max(0, offset - limit);
            load();
        });
        btnNext.addActionListener(e -> {
            offset = offset + limit;
            load();
        });

        btnDetail.addActionListener(e -> withSelectedProductId(pid -> {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                ProductWithMetrics detail;

                @Override
                protected Void doInBackground() {
                    detail = productService.getDetailAndAddView(pid);
                    return null;
                }

                @Override
                protected void done() {
                    if (detail == null) return;
                    JOptionPane.showMessageDialog(ProductsPanel.this,
                            String.format("#%d %s%n가격: %,d%n상태: %s%n좋아요=%d, 조회=%d%n판매자=%s (%s)",
                                    detail.productId(), detail.name(), detail.price(), detail.status(), detail.likeCount(), detail.viewCount(),
                                    detail.seller() == null ? "?" : detail.seller().sellerName(), detail.seller() == null ? "?" : detail.seller().sellerEmail()),
                            "상품 상세", JOptionPane.INFORMATION_MESSAGE);
                    load();
                }
            };
            w.execute();
        }));

        btnLike.addActionListener(e -> withSelectedProductId(pid -> {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    productService.like(pid);
                    return null;
                }

                @Override
                protected void done() {
                    load();
                }
            };
            w.execute();
        }));
        btnUnlike.addActionListener(e -> withSelectedProductId(pid -> {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    productService.unlike(pid);
                    return null;
                }

                @Override
                protected void done() {
                    load();
                }
            };
            w.execute();
        }));

        btnReserve.addActionListener(e -> withSelectedProductId(pid -> {
            Long buyerId = Session.get().getUserId();
            if (buyerId == null) {
                JOptionPane.showMessageDialog(this, "로그인 후 예약할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                Exception err;

                @Override
                protected Void doInBackground() {
                    try {
                        orderService.requestReservation(pid, buyerId);
                    } catch (Exception ex) {
                        err = ex;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (err != null)
                        JOptionPane.showMessageDialog(ProductsPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    else
                        JOptionPane.showMessageDialog(ProductsPanel.this, "예약 요청 완료", "정보", JOptionPane.INFORMATION_MESSAGE);
                    load();
                }
            };
            w.execute();
        }));

        btnShowReviews.addActionListener(e -> withSelectedProductId(pid -> {
            SwingWorker<Void,Void> w = new SwingWorker<>(){
                Exception err;
                jdbc.common.paging.Page<jdbc.domain.review.dto.ReviewDto> page;
                @Override protected Void doInBackground(){
                    try{ page = reviewService.findByProductId(pid, 20, 0); } catch(Exception ex){ err = ex; } return null; }
                @Override protected void done(){
                    if (err!=null){ JOptionPane.showMessageDialog(ProductsPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); return; }
                    java.util.List<jdbc.domain.review.dto.ReviewDto> rows = page==null?java.util.List.of():page.content();
                    StringBuilder sb = new StringBuilder();
                    for (var r: rows){
                        sb.append(String.format("[%d] %s\n평점=%s\n%s\n작성자=%d 작성시간=%s\n\n", r.reviewId(), r.title(), r.rating(), r.content(), r.reviewerId(), r.createdAt()));
                    }
                    if (sb.length()==0) sb.append("리뷰 없음");
                    JOptionPane.showMessageDialog(ProductsPanel.this, sb.toString(), "리뷰", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            w.execute();
        }));

        btnDelete.addActionListener(e -> withSelectedProductId(pid -> {
            Long sellerId = Session.get().getUserId();
            if (sellerId == null) {
                JOptionPane.showMessageDialog(this, "로그인 후 삭제할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                Exception err;
                boolean deleted = false;

                @Override
                protected Void doInBackground() {
                    try {
                        Long ownerId = productService.findSellerId(pid);
                        if (ownerId == null) throw new RuntimeException("상품 없음");
                        if (!sellerId.equals(ownerId)) throw new RuntimeException("삭제 권한 없음");
                        deleted = productService.delete(pid);
                    } catch (Exception ex) {
                        err = ex;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (err != null)
                        JOptionPane.showMessageDialog(ProductsPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    else if (deleted) {
                        JOptionPane.showMessageDialog(ProductsPanel.this, "상품 삭제(소프트) 완료", "정보", JOptionPane.INFORMATION_MESSAGE);
                        load();
                    } else {
                        JOptionPane.showMessageDialog(ProductsPanel.this, "삭제 실패", "경고", JOptionPane.WARNING_MESSAGE);
                        load();
                    }
                }
            };
            w.execute();
        }));

        btnFavorite.addActionListener(e -> withSelectedProductId(pid -> {
            Long userId = Session.get().getUserId();
            if (userId == null) { JOptionPane.showMessageDialog(this, "로그인 후 찜할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE); return; }
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                Exception err;
                boolean newActive = false;
                @Override protected Void doInBackground() {
                    try { newActive = favoriteService.toggleFavorite(userId, pid); } catch(Exception ex) { err = ex; } return null; }
                @Override protected void done() { if (err!=null) JOptionPane.showMessageDialog(ProductsPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); else JOptionPane.showMessageDialog(ProductsPanel.this, newActive ? "찜 추가 완료" : "찜 취소 완료", "정보", JOptionPane.INFORMATION_MESSAGE); load(); }
            };
            w.execute();
        }));

        // selection listener to update favorite button label
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateFavoriteButton();
        });

        // session 변화에 반응: 로그인/로그아웃 시 UI 갱신
        Session.Listener listener = new Session.Listener() {
            @Override
            public void onLogin(jdbc.domain.user.model.User user) {
                updateForSession();
            }

            @Override
            public void onLogout() {
                updateForSession();
            }
        };
        Session.get().addListener(listener);

    // 초기 상태
    updateForSession();

        load();
    }

    private void updateForSession() {
        // 간단히 선택된 행의 판매자 id와 현재 세션 사용자의 id를 비교해 삭제 버튼 활성화/비활성화
        // 비활성화로 기본 보안 제공
        // (선택 상품이 없으면 버튼 비활성)
        int row = table.getSelectedRow();
        boolean enable = false;
        if (row >= 0) {
            try {
                var p = tableModel.rows.get(row);
                Long sellerId = p.seller() == null ? null : p.seller().sellerId();
                Long me = Session.get().getUserId();
                enable = me != null && sellerId != null && me.equals(sellerId);
            } catch (Exception ignored) {
                enable = false;
            }
        }
        // find delete button in bottom panel (assumes it exists)
        for (Component c : ((JPanel) getComponent(getComponentCount() - 1)).getComponents()) {
            if (c instanceof JButton && "삭제 (판매자)".equals(((JButton) c).getText())) {
                c.setEnabled(enable);
            }
        }
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        int row = table.getSelectedRow();
        if (row < 0) {
            btnFavorite.setText("찜/취소");
            return;
        }
        Long id = tableModel.getIdAt(row);
        boolean fav = favoriteSet.contains(id);
        btnFavorite.setText(fav ? "찜 취소" : "찜");
    }

    private void withSelectedProductId(LongConsumer fn) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "항목을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long id = tableModel.getIdAt(row);
        fn.accept(id);
    }

    private void load() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            Page<ProductWithMetrics> page;
            Exception err;

            @Override
            protected Void doInBackground() {
                try {
                    String q = tfSearch.getText().trim();
                    if (q.isEmpty()) page = productService.getPage(limit, offset);
                    else page = productService.searchPage(limit, offset, q, null, null, (ProductStatus) null);
                    // load favorites for current user (one page large enough)
                    Long me = Session.get().getUserId();
                    favoriteSet.clear();
                    if (me != null) {
                        var favs = favoriteService.findByUser(me, 1000, 0);
                        for (var f : favs) favoriteSet.add(f.productId());
                    }
                } catch (Exception ex) {
                    err = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (err != null) {
                    JOptionPane.showMessageDialog(ProductsPanel.this, err.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tableModel.setData(page == null ? List.of() : page.content());
                updateFavoriteButton();
            }
        };
        w.execute();
    }

    class ProductsTableModel extends AbstractTableModel {
    private final String[] cols = {"찜","ID", "이름", "가격", "상태", "좋아요", "조회", "판매자"};
        private List<ProductWithMetrics> rows = new ArrayList<>();

        public void setData(List<ProductWithMetrics> data) {
            this.rows = data;
            fireTableDataChanged();
        }

        public Long getIdAt(int r) {
            return rows.get(r).productId();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            ProductWithMetrics p = rows.get(r);
            return switch (c) {
                case 0 -> (favoriteSet.contains(p.productId()) ? "★" : "");
                case 1 -> p.productId();
                case 2 -> p.name();
                case 3 -> p.price();
                case 4 -> p.status();
                case 5 -> p.likeCount();
                case 6 -> p.viewCount();
                case 7 -> p.seller() == null ? "?" : p.seller().sellerName();
                default -> "";
            };
        }
    }

    @FunctionalInterface
    interface LongConsumer {
        void accept(Long id);
    }
}
