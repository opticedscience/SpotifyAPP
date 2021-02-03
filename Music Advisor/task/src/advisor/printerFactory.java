package advisor;

import java.util.ArrayList;
import java.util.List;

public class printerFactory {
    int itemPerPage;
    Auth auth;
    String listName;

    public printerFactory(Auth auth, int itemPerPage){
        this.auth=auth;
        this.itemPerPage = itemPerPage;

    }

    public void setItemPerPage(int itemPerPage) {
    }

    public void setListName(String listName) {
        this.listName=listName;
    }


    public paginatedPrinter selectPrinter(printListType selectType) {
        FindResource findResource=new FindResource(auth);
        List<String> response= new ArrayList<>();

        switch (selectType) {
            case FEATURED:
                findResource.setStrategy(new findFeaturedStrategy());
                response=findResource.findResource();
                return new paginatedPrinter(response,itemPerPage);

            case PLAYLIST:
                findResource.setStrategy(new findPlayListStrategy(auth,listName));
                response=findResource.findResource();
                return new paginatedPrinter(response,itemPerPage);


            case CATEGORIES:
                findResource.setStrategy(new findCategoriesStrategy());
                response=findResource.findResource();
                return new paginatedPrinter(response,itemPerPage);

            case NEWRELEASE:
                findResource.setStrategy(new findNewReleaseStrategy());
                response=findResource.findResource();
                return new paginatedPrinter(response,itemPerPage);

            default:
                return null;


        }

    }
}
