package net.macu.browser.handler;

import net.macu.browser.Client;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Arrays;
import java.util.UUID;

public class MessageRouterHandler extends CefMessageRouterHandlerAdapter {
    public static final String QUERY_FUNCTION_NAME = "macuQuery";
    public static final String CANCEL_FUNCTION_NAME = "macuCancel";
    private final Client client;

    public MessageRouterHandler(Client client) {
        this.client = client;
    }

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        try {
            JSONObject result = (JSONObject) new JSONParser().parse(request);
            UUID uuid = UUID.fromString((String) result.get("uuid"));
            JSONArray jsonPaths = (JSONArray) result.get("paths");
            String[] paths = new String[jsonPaths.size()];
            for (int i = 0; i < jsonPaths.size(); i++) {
                paths[i] = (String) jsonPaths.get(i);
            }
            System.out.println("got result for query " + uuid + " with length " + Arrays.toString(paths));
            client.getResultCompletableFuture(uuid).complete(paths);
        } catch (Exception e) {
            e.printStackTrace();
            callback.failure(1, e.getMessage());
        }
        callback.success("");
        return true;
    }

    @Override
    public void onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId) {
    }
}
