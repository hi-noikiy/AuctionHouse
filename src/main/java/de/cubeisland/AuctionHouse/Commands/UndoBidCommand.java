package de.cubeisland.AuctionHouse.Commands;

import de.cubeisland.AuctionHouse.AbstractCommand;
import de.cubeisland.AuctionHouse.Arguments;
import de.cubeisland.AuctionHouse.Auction.Bidder;
import static de.cubeisland.AuctionHouse.AuctionHouse.t;
import de.cubeisland.AuctionHouse.BaseCommand;
import de.cubeisland.AuctionHouse.Manager;
import de.cubeisland.AuctionHouse.Perm;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class UndoBidCommand extends AbstractCommand
{
    public UndoBidCommand(BaseCommand base)
    {
        super(base, "undobid");
    }

    public boolean execute(CommandSender sender, String[] args)
    {
        if (!Perm.get().check(sender,"auctionhouse.command.undobid")) return true;
        if (args.length < 1)
        {
            sender.sendMessage(t("undo_title1"));
            sender.sendMessage(t("undo_title2"));
            sender.sendMessage(t("undo_title3"));
            sender.sendMessage("");
            return true;
        }
        Arguments arguments = new Arguments(args);
        Player psender = (Player) sender;
        if (arguments.getString("1").equals("last"))
        {
            if (Bidder.getInstance(psender).getlastAuction(Bidder.getInstance(psender)) == null)
            {
                sender.sendMessage(t("pro")+" "+t("undo_pro"));
                return true;
            }
            if (Bidder.getInstance(psender).getlastAuction(Bidder.getInstance(psender)).undobid(Bidder.getInstance(psender)))
            {
                sender.sendMessage(t("i")+" "+t("undo_redeem"));
                return true;
            }
            else
            {
                sender.sendMessage(t("pro")+" "+t("undo_pro"));
                return true;
            }
        }
        if (arguments.getInt("1") != null)
        {
            if (Manager.getInstance().getAuction(arguments.getInt("1")) == null)
            {
                sender.sendMessage(t("e")+" "+t("auction_no_exist",arguments.getInt("1")));
                return true;
            }
            if (Manager.getInstance().getAuction(arguments.getInt("1")).undobid(Bidder.getInstance(psender)))
            {
                sender.sendMessage(t("i")+" "+t("undo_bid_n",arguments.getInt("1")));
                return true;
            }
            else
            {
                sender.sendMessage(t("e")+" "+t("undo_bidder"));
                return true;
            }
        }
        sender.sendMessage(t("e")+" "+t("undo_fail"));
        return true;
    }

    @Override
    public String getUsage()
    {
        return super.getUsage() + " last";
    }

    public String getDescription()
    {
        return t("command_undo");
    }
}
