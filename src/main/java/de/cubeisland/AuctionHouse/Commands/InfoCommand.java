package de.cubeisland.AuctionHouse.Commands;

import de.cubeisland.AuctionHouse.Perm;
import de.cubeisland.AuctionHouse.AuctionManager;
import de.cubeisland.AuctionHouse.BaseCommand;
import de.cubeisland.AuctionHouse.ServerBidder;
import de.cubeisland.AuctionHouse.AuctionHouseConfiguration;
import de.cubeisland.AuctionHouse.AuctionHouse;
import de.cubeisland.AuctionHouse.Bidder;
import de.cubeisland.AuctionHouse.Auction;
import static de.cubeisland.AuctionHouse.Translation.Translator.t;
import de.cubeisland.AuctionHouse.AbstractCommand;
import de.cubeisland.AuctionHouse.Arguments;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class InfoCommand extends AbstractCommand
{
    
    private static final AuctionHouse plugin = AuctionHouse.getInstance();
    private static final AuctionHouseConfiguration config = plugin.getConfigurations();
    Economy econ = plugin.getEconomy();
    
    public InfoCommand(BaseCommand base)
    {
        super(base, "info");
    }

    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage("/ah info <AuctionID>");
            sender.sendMessage("/ah info <Player>");
            sender.sendMessage("/ah info Bids");
            sender.sendMessage("/ah info lead");
            sender.sendMessage("/ah info own");
            sender.sendMessage("/ah info *Server");
            return true;
        }
        Arguments arguments = new Arguments(args);
        if (!Perm.get().check(sender,"auctionhouse.info"))
        {
            
            return true;
        }

        if (arguments.getString("1").equalsIgnoreCase("Bids"))//bidding
        {
            List<Auction> auctions = Bidder.getInstance((Player) sender).getAuctions();
            int max = auctions.size();
            if (max == 0)
            {
                sender.sendMessage(t("i")+" "+t("info_no_bid"));
            }
            for (int i = 0; i < max; ++i)
            {
                Auction auction = auctions.get(i);
                if (auction.owner != (Player) sender)
                {
                    this.sendInfo(sender, auction);
                }
            }
        }
        else
        {
            if (arguments.getString("1").equalsIgnoreCase("own"))
            {
                List<Auction> auctions = Bidder.getInstance((Player) sender).getOwnAuctions();
                int max = auctions.size();
                if (max == 0)
                {
                    sender.sendMessage(t("i")+" "+t("info_no_start"));
                }
                for (int i = 0; i < max; ++i)
                {
                    Auction auction = auctions.get(i);
                    this.sendInfo(sender, auction);
                }
            }
            else
            {

                if (arguments.getString("1").equalsIgnoreCase("lead"))
                {
                    List<Auction> auctions = Bidder.getInstance((Player) sender).getLeadingAuctions();
                    int max = auctions.size();
                    AuctionHouse.debug("max: " + max);
                    if (max == 0)
                    {
                        sender.sendMessage(t("i")+" "+t("info_no_lead"));
                    }
                    for (int i = 0; i < max; ++i)
                    {
                        Auction auction = auctions.get(i);
                        this.sendInfo(sender, auction);
                    }
                }
                else
                {
                    if (arguments.getString("1").equalsIgnoreCase("*Server"))
                    {
                        List<Auction> auctions = ServerBidder.getInstance().getAuctions();
                        int max = auctions.size();
                        AuctionHouse.debug("max: " + max);
                        if (max == 0)
                        {
                            sender.sendMessage(t("i")+" "+t("info_no_serv"));
                        }
                        for (int i = 0; i < max; ++i)
                        {
                            Auction auction = auctions.get(i);
                            this.sendInfo(sender, auction);
                        }
                    }
                    else
                    {
                        Integer id = arguments.getInt("1");
                        if (id != null)
                        {
                            if (AuctionManager.getInstance().getAuction(id) != null)
                            {
                                this.sendInfo(sender, AuctionManager.getInstance().getAuction(id));
                            }
                            else
                            {
                                sender.sendMessage(t("i")+" "+t("auction_no_exist",id));
                            }
                        }
                        else
                        {
                            if (!Perm.get().check(sender,"auctionhouse.info.others")) return true;
                            Bidder player = arguments.getBidder("1");
                            if (player != null)
                            {
                                AuctionHouse.debug("Player Auction");
                                List<Auction> auctions = player.getAuctions(player);
                                int max = auctions.size();
                                AuctionHouse.debug("max: " + max);
                                if (max == 0)
                                {
                                    sender.sendMessage(t("e")+t("info_no_auction",player.getName()));
                                }
                                for (int i = 0; i < max; ++i)
                                {
                                    Auction auction = auctions.get(i);
                                    this.sendInfo(sender, auction);
                                }
                            }
                            else
                            {
                                sender.sendMessage(t("perm")+" "+t("info_p_no_auction",arguments.getString("1")));
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public void sendInfo(CommandSender sender, Auction auction)
    {
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
            output +=" "+t("info_out_with",econ.format(auction.bids.peek().getAmount()));
        }
        output += " "+t("info_out_end",
                        DateFormatUtils.format(auction.auctionEnd, config.auction_timeFormat));

        sender.sendMessage(output);
    }

    @Override
    public String getUsage()
    {
        return super.getUsage() + " <<AuctionId>|<Player>> )";
    }

    public String getDescription()
    {
        return t("command_info");
    }
}