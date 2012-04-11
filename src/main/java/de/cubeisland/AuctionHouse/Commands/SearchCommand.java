package de.cubeisland.AuctionHouse.Commands;

import de.cubeisland.AuctionHouse.Perm;
import de.cubeisland.AuctionHouse.Arguments;
import de.cubeisland.AuctionHouse.AuctionManager;
import de.cubeisland.AuctionHouse.BaseCommand;
import de.cubeisland.AuctionHouse.AuctionSort;
import de.cubeisland.AuctionHouse.ServerBidder;
import de.cubeisland.AuctionHouse.AbstractCommand;
import de.cubeisland.AuctionHouse.AuctionHouseConfiguration;
import de.cubeisland.AuctionHouse.AuctionHouse;
import de.cubeisland.AuctionHouse.Auction;
import static de.cubeisland.AuctionHouse.Translation.Translator.t;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author Faithcaio
 */
public class SearchCommand extends AbstractCommand
{
    
    private static final AuctionHouse plugin = AuctionHouse.getInstance();
    private static final AuctionHouseConfiguration config = plugin.getConfigurations();
    Economy econ = AuctionHouse.getInstance().getEconomy();
    
    public SearchCommand(BaseCommand base)
    {
        super(base, "search");
    }

    public boolean execute(CommandSender sender, String[] args)
    {
        if (!Perm.get().check(sender,"auctionhouse.search")) return true;
        if (args.length < 1)
        {
            sender.sendMessage("/ah search <Item> [s:<date|id|price>]");
            return true;
        }
        Arguments arguments = new Arguments(args);
        List<Auction> auctionlist;

        if (arguments.getString("1") == null)
        {
            if (arguments.getString("s")!=null)
               sender.sendMessage(t("pro")+" "+t("search_pro")); 
            sender.sendMessage(t("e")+" "+t("invalid_com"));
            return true;
        }
        if (arguments.getMaterial("1") == null)
        {
            sender.sendMessage(t("e")+" "+t("item_no_exist",arguments.getString("1")));
            return true;
        }
        auctionlist = AuctionManager.getInstance().getAuctionItems(arguments.getMaterial("1"));
        if (arguments.getString("s") != null)
        {
            AuctionSort sorter = new AuctionSort();
            if (arguments.getString("s").equalsIgnoreCase("date"))
            {
                sorter.SortAuction(auctionlist, "date");
            }
            if (arguments.getString("s").equalsIgnoreCase("id"))
            {
                sorter.SortAuction(auctionlist, "id");
            }
            if (arguments.getString("s").equalsIgnoreCase("price"))
            {
                sorter.SortAuction(auctionlist, "price");
            }
        }
        if (auctionlist.isEmpty())
        {
            sender.sendMessage(t("i")+" "+t("search_found"));
        }
        this.sendInfo(sender, auctionlist);
        return true;
    }

    public void sendInfo(CommandSender sender, List<Auction> auctionlist)
    {
        int max = auctionlist.size();
        for (int i = 0; i < max; ++i)
        {
            Auction auction = auctionlist.get(i);
            String output = "";
            output += "#" + auction.id + ": ";
            output += auction.item.toString();
            if (auction.item.getEnchantments().size() > 0)
            {
                output += " "+t("info_out_ench")+" ";
                for (Enchantment enchantment : auction.item.getEnchantments().keySet())
                {
                    output += enchantment.toString() + ":";
                    output += auction.item.getEnchantments().get(enchantment).toString() + " ";
                }
            }
            if (auction.bids.peek().getBidder().equals(auction.owner))
            {
                output += " "+t("info_out_bid",econ.format(auction.bids.peek().getAmount()));
            }
            else
            {
                if (auction.bids.peek().getBidder() instanceof ServerBidder)
                {
                    output += t("info_out_leadserv");
                }
                else
                {
                    output += t("info_out_lead",auction.bids.peek().getBidder().getName());
                }
                output += " "+t("with",auction.bids.peek().getAmount());
            }
            output += " "+t("info_out_end",
                    DateFormatUtils.format(auction.auctionEnd, config.auction_timeFormat));

            sender.sendMessage(output);
        }
    }

    @Override
    public String getDescription()
    {
        return t("command_search");
    }

    @Override
    public String getUsage()
    {
        return super.getUsage() + " <Item> [s:<date|id|price>]";
    }
}