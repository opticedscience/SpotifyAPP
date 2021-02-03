package advisor;

import java.util.ArrayList;
import java.util.List;

public class paginatedPrinter {

    private List<String> responseList = new ArrayList<>();
    private int itemPerPage=0;
    private int totalPage=0;
    private int currentPage=0;

    public paginatedPrinter(List<String> responseList, int itemPerPage) {
        this.responseList=responseList;
        this.itemPerPage=itemPerPage;
        if(responseList!=null){
            totalPage=responseList.size()/itemPerPage;
            if (responseList.size() % itemPerPage > 0) {
                totalPage++;
            }
            printPage(1);}

    }

    public void printPage(int pageDirection) {
        int nextPage=currentPage+pageDirection;
        int offset=0;

        if (nextPage <= 0 || nextPage > totalPage) {
            System.out.println("No more pages.");
            return;
        }
        if(pageDirection>0){
            offset=currentPage*itemPerPage;
        }
        else{offset=(currentPage-2)*itemPerPage;}

        int lastPage=Math.min( offset + itemPerPage,responseList.size());

        for (int i = offset; i < lastPage; i++) {
            System.out.println(responseList.get(i));
        }
        currentPage=nextPage;
        System.out.printf("---PAGE %d OF %d---\n",currentPage,totalPage);
    }
}
