package de.cubeisland.AuctionHouse.Auction;

import de.cubeisland.AuctionHouse.AuctionHouse;
import static de.cubeisland.AuctionHouse.AuctionHouse.t;
import de.cubeisland.AuctionHouse.AuctionHouseConfiguration;
import de.cubeisland.AuctionHouse.Database.Database;
import de.cubeisland.AuctionHouse.Manager;
import de.cubeisland.AuctionHouse.Perm;
import java.util.Stack;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an auction
 *
 * @author Faithcaio
 */
public class Auction
{
    private int id;
    private final ItemStack item;
    private Bidder owner;
    private final long auctionEnd;
    private final Stack<Bid> bids;
    private static final AuctionHouse plugin = AuctionHouse.getInstance();
    private static final AuctionHouseConfiguration config = plugin.getConfiguration();
    private final Database db;
    
/**
 * Creates an new auction
 */
    public Auction(ItemStack item, Bidder owner, long auctionEnd, double startBid)
    {
        this.db = AuctionHouse.getInstance().getDB();
        this.id = Manager.getInstance().getFreeIds().pop();
        this.item = item;
        this.owner = owner;
        this.auctionEnd = auctionEnd;
        this.bids = new Stack<Bid>();
        this.bids.push(new Bid(owner, startBid, this));
    }

/**
 * Load in auction from DataBase
 */
    public Auction(int id,ItemStack item, Bidder owner, long auctionEnd)
    {
        Manager.getInstance().getFreeIds().removeElement(id);
        this.db = AuctionHouse.getInstance().getDB();
        this.id = id;
        this.item = item;
        this.owner = owner;
        this.auctionEnd = auctionEnd;
        this.bids = new Stack<Bid>();
    }

/**
 * Adds a bid to auction
 * @return true if bidded succesfully
 */
    public boolean bid(final Bidder bidder, final double amount)//evtl nicht bool / bessere Unterscheidung
    {
        if (amount <= 0)
        {
            bidder.getPlayer().sendMessage(t("e")+" "+t("auc_bid_low1"));
            return false;
        }
        if (amount <= this.bids.peek().getAmount())
        {
            bidder.getPlayer().sendMessage(t("i")+" "+t("auc_bid_low2"));
            return false;
        }
        if ((AuctionHouse.getInstance().getEconomy().getBalance(bidder.getName()) >= amount)
                || Perm.get().check(bidder,"auctionhouse.command.bid.infinite"))
        {
            if (AuctionHouse.getInstance().getEconomy().getBalance(bidder.getName()) - bidder.getTotalBidAmount() >= amount
                    || Perm.get().check(bidder,"auctionhouse.command.bid.infinite"))
            {
                this.bids.push(new Bid(bidder, amount, this));
                return true;
            }
            bidder.getPlayer().sendMessage(t("e")+" "+t("auc_bid_money1"));
            return false;
        }
        bidder.getPlayer().sendMessage(t("e")+" "+t("auc_bid_money2"));
        return false;
    }
    
/**
 * reverts a bid if allowed
 * @return true if reverted succesfully
 */
    public boolean undobid(final Bidder bidder)
    {
        if (bidder != this.bids.peek().getBidder())
        {
            bidder.getPlayer().sendMessage(t("e")+" "+t("undo_bidder"));
            return false;
        }
        if (bidder == this.owner)
        {
            bidder.getPlayer().sendMessage(t("pro")+" "+t("undo_pro2"));
            return false;
        }
        long undoTime = config.auction_undoTime;
        if (undoTime < 0) //Infinite UndoTime
        {
            undoTime = this.auctionEnd - this.bids.peek().getTimestamp();
        }
        if ((System.currentTimeMillis() - this.bids.peek().getTimestamp()) > undoTime)
        {
            bidder.getPlayer().sendMessage(t("e")+" "+t("undo_time"));
            return false;
        }
        //else: Undo Last Bid
        db.execUpdate("DELETE FROM `bids` WHERE `bidderid`=? && `auctionid`=? && `timestamp`=?"
                      ,bidder.getId(), this.id, this.bids.peek().getTimestamp());
        this.bids.pop();
        return true;
    }

/**
 * @return id as int
 */   
    public int getId()
    {
       return this.id; 
    }
    
/**
 * sets the AuctionID
 * @param int id
 */   
    public void setId(int id)
    {
        this.id = id;
    }
    
/**
 * @return item as ItemStack
 */       
    public ItemStack getItem()
    {
        return this.item;
    }   
    
/**
 * @return owner as Bidder
 */      
    public Bidder getOwner()
    {
        return this.owner;
    }
    
/**
 * @return auctionEnd in Milliseconds
 */     
    public long getAuctionEnd()
    {
        return this.auctionEnd;
    }
    
/**
 * @return all bids
 */       
    public Stack<Bid> getBids()
    {
        return this.bids;
    }
    
/**
 * gives owner (and last/initial bid) to Server
 */    
    public void giveServer()
    {
        this.owner = ServerBidder.getInstance();
        this.bids.peek().giveServer();
    }
}