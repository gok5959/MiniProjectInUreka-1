package jdbc.common.paging;

import java.util.ArrayList;
import java.util.List;

public record Page<T>(List<T> content, int total, int limit, int offset) {

    public int totalPages() {
        return limit <= 0 ? 0 : (int) Math.ceil((double) total / (double) limit);
    }

    public int currentPage() {
        if (limit <= 0 || total == 0) return 0;
        return (offset / limit) + 1;
    }

    public boolean hasNext() {
        int cp = currentPage();
        int tp = totalPages();
        return cp > 0 && cp < tp;
    }

    public boolean hasPrev() {
        int cp = currentPage();
        return cp > 1;
    }

    public int offsetOfPage(int page) {
        if (limit <= 0) return 0;
        if (page < 1) page = 1;
        int tp = totalPages();
        if (tp == 0) return 0;
        if (page > tp) page = tp;
        return (page - 1) * limit;
    }

    public int blockStart(int blockSize) {
        int cp = currentPage();
        int tp = totalPages();
        if (blockSize <= 0 || cp == 0 || tp == 0) return 0;
        int blockIndex = (cp - 1) / blockSize;              // 0-based
        return blockIndex * blockSize + 1;                  // 1-based page
    }

    public int blockEnd(int blockSize) {
        int tp = totalPages();
        int bs = blockStart(blockSize);
        if (bs == 0) return 0;
        return Math.min(bs + blockSize - 1, tp);
    }

    public boolean hasPrevBlock(int blockSize) {
        int bs = blockStart(blockSize);
        return bs > 1;
    }

    public boolean hasNextBlock(int blockSize) {
        int be = blockEnd(blockSize);
        int tp = totalPages();
        return be > 0 && be < tp;
    }

    public List<Integer> pagesInCurrentBlock(int blockSize) {
        int bs = blockStart(blockSize);
        int be = blockEnd(blockSize);
        List<Integer> pages = new ArrayList<>();
        if (bs == 0 || be == 0) return pages;
        for (int p = bs; p <= be; p++) pages.add(p);
        return pages;
    }
}
