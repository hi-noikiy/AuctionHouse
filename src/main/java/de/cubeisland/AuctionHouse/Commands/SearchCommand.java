package de.cubeisland.AuctionHouse.Commands;

import de.cubeisland.AuctionHouse.Auction.Auction;
import de.cubeisland.AuctionHouse.*;
import static de.cubeisland.AuctionHouse.AuctionHouse.t;
import java.util.Collections;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class SearchCommand extends AbstractCommand
{
    
    private static final AuctionHouse plugin = AuctionHouse.getInstance();
    private static final AuctionHouseConfiguration config = plugin.getConfiguration();
    Economy econ = AuctionHouse.getInstance().getEconomy();
    
    public SearchCommand(BaseCommand base)
    {
        super(base, "search");
    }

    public boolean execute(CommandSender sender, CommandArgs args)
    {
        if (!Perm.get().check(sender,"auctionhouse.command.search")) return true;
        if (args.isEmpty())
        {
            sender.sendMessage(t("search_title1"));
            sender.sendMessage(t("search_title2"));
            sender.sendMessage("");
            return true;
        }
        List<Auction> auctionlist;

        if (args.getString("1") == null)
        {
            if (args.getString("s")!=null)
               sender.sendMessage(t("pro")+" "+t("search_pro")); 
            sender.sendMessage(t("e")+" "+t("invalid_com"));
            return true;
        }
        if (args.getItem("1") == null)
        {
            sender.sendMessage(t("e")+" "+t("item_no_exist",args.getString("1")));
            return true;
        }
        auctionlist = Manager.getInstance().getAuctionItem(args.getItem("1"));
        if (args.getString("s") != null)
        {
            if (args.getString("s").equalsIgnoreCase("date"))
            {
                AuctionSort.sortAuction(auctionlist, "date");
                Collections.reverse(auctionlist);
            }
            if (args.getString("s").equalsIgnoreCase("id"))
            {
                AuctionSort.sortAuction(auctionlist, "id");
                Collections.reverse(auctionlist);
            }
            if (args.getString("s").equalsIgnoreCase("price"))
            {
                AuctionSort.sortAuction(auctionlist, "date");
                AuctionSort.sortAuction(auctionlist, "price");
                Collections.reverse(auctionlist);
            }
        }
        if (auctionlist.isEmpty())
        {
            sender.sendMessage(t("i")+" "+t("search_found"));
        }
        Util.sendInfo(sender, auctionlist);
        return true;
    }

    @Override
    public String getDescription()
    {
        return t("command_search");
    }

    @Override
    public String getUsage()
    {
        return super.getUsage() + " <Item>";
    }
}
