package service;

import tw.yukina.notion.sdk.client.NotionClient;
import tw.yukina.notion.sdk.client.NotionClientImpl;
import tw.yukina.notion.sdk.model.database.Database;
import tw.yukina.notion.sdk.model.page.Page;

public class NotionUpdater {

    public void updateTransactions() {
        NotionClient notionClient = new NotionClientImpl("test-token");
        Page page = notionClient.getPageByUuid("yourPageUuid");
        Database database = notionClient.getDatabaseByUuid("test");


    }
}
