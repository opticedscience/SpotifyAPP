package advisor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    static String accessPoint="https://accounts.spotify.com";
    static String resource="https://api.spotify.com";
    static int page=0;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 1) {
            if ("-access".equals(args[0])) {
                accessPoint=args[1];
            }
        }
        if (args.length > 3) {
            if ("-resource".equals(args[2])) {
                resource=args[3];
            }
        }

        if (args.length > 5) {
            if ("-page".equals(args[4])) {
                page=Integer.valueOf(args[5]);
            }
        }

        Auth auth=null;
        printerFactory printerFactory=new printerFactory(auth,page);
        paginatedPrinter paginatedPrinter=new paginatedPrinter(null,page);

        Scanner scanner = new Scanner(System.in);
        boolean flag=true;
        boolean isAuthorized=false;

        while (flag) {
            String input=scanner.nextLine();
            if(!isAuthorized && !input.equals("auth") && !input.equals("exit")){
                System.out.println("Please, provide access for application.");
            }
            else {
            switch (input) {
                case "auth":
                    auth=Auth.getInstance(accessPoint,resource);
                    printerFactory=new printerFactory(auth,page);
//                    Authorization authorization=new Authorization();
//                    authorization.authorize(accessPoint);
//                    authorization.getAccessToken(accessPoint);
                    isAuthorized=true;
                    break;
                case "new":
                    paginatedPrinter=printerFactory.selectPrinter(printListType.NEWRELEASE);

                    break;
                case "featured":
                    paginatedPrinter=printerFactory.selectPrinter(printListType.FEATURED);

                    break;
                case "categories":
                    paginatedPrinter=printerFactory.selectPrinter(printListType.CATEGORIES);

                    break;
                case "prev":
                    paginatedPrinter.printPage(-1);
                    break;
                case "next":
                    paginatedPrinter.printPage(1);
                    break;
                default:
                    if (input.contains("playlists")){
                    String catName=input.replaceAll("playlists ","");
                    printerFactory.setListName(catName);
                    paginatedPrinter=printerFactory.selectPrinter(printListType.PLAYLIST);
                    }
                    else{
                        System.out.println("Wrong inputs");
                    }
                    break;
                case "exit":
                    System.out.println("---GOODBYE!---");
                    flag=false;
            }
        }
        }
    }
}
