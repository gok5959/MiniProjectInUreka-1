import jdbc.domain.user.dao.UserDao;
import jdbc.domain.user.service.UserService;
import swing.ui.ContextBar;
import swing.ui.Session;
import swing.ui.MainWindow;
import swing.ui.panels.BuyerOrdersPanel;
import swing.ui.panels.ProductsPanel;
import swing.ui.panels.SellerAdminPanel;
import jdbc.domain.product.service.ProductService;
import jdbc.domain.order.service.OrderService;
import java.awt.Font;
import java.io.IOException;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			// 전역 룩앤필: Nimbus 시도, 없으면 기본
			try {
				for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
				// 전역 폰트 크기 조정
				Font defaultFont = UIManager.getFont("Label.font");
				if (defaultFont != null) {
					Font f = defaultFont.deriveFont((float)14);
					java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
					while (keys.hasMoreElements()) {
						Object k = keys.nextElement();
						Object v = UIManager.get(k);
						if (v instanceof javax.swing.plaf.FontUIResource) {
							UIManager.put(k, new javax.swing.plaf.FontUIResource(f));
						}
					}
				}
			} catch (Exception ignored) {}
			UserDao userDao = null;
            UserService userService = null;
            try {
                userService = new UserService(userDao);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ContextBar contextBar = new ContextBar(userService);

			ProductService productService = new ProductService();
			OrderService orderService = new OrderService();
	            jdbc.domain.review.service.ReviewService reviewService = new jdbc.domain.review.service.ReviewService();

			JTabbedPane tabs = new JTabbedPane();
			tabs.add("상품 목록", new ProductsPanel(productService, orderService, reviewService));
			// BuyerOrdersPanel needs reviewService to allow writing reviews
			tabs.add("구매자 주문", new BuyerOrdersPanel(orderService, reviewService));
			tabs.add("찜 목록", new swing.ui.panels.FavoritesPanel());

			// admin tab은 ADMIN 권한일 때만 보이도록 처리
			SellerAdminPanel sellerAdminPanel = new SellerAdminPanel(orderService);
			tabs.add("판매자/관리자 주문", sellerAdminPanel);

			// 로그인한 사용자(판매자 포함)가 탭을 볼 수 있도록 변경
			Session.get().addListener(new Session.Listener() {
				@Override public void onLogin(jdbc.domain.user.model.User user) {
					// 로그인 시에는 판매자/관리자 탭을 항상 노출(권한에 따라 패널 내부에서 기능을 제한)
					int idx = tabs.indexOfComponent(sellerAdminPanel);
					if (idx < 0) tabs.add("판매자/관리자 주문", sellerAdminPanel);
				}
				@Override public void onLogout() {
					int idx = tabs.indexOfComponent(sellerAdminPanel);
					if (idx >= 0) tabs.remove(idx);
					if (tabs.getTabCount() > 0) tabs.setSelectedIndex(0);
				}
			});

			MainWindow w = new MainWindow(contextBar, tabs);
			w.setVisible(true);
		});
	}
}
