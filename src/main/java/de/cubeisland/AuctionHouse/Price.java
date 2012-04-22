package de.cubeisland.AuctionHouse;

import de.cubeisland.AuctionHouse.Database.Database;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class Price {
    
    private Map<ItemStack, Double> price = new HashMap<ItemStack, Double>();
    private Map<ItemStack, Integer> amount = new HashMap<ItemStack, Integer>();
    
    public Price(){}
    
    public double getPrice(ItemStack item)
    {
        if (this.price.get(item)==null)
            return 0;
        return this.price.get(item);
    }
    
    public double adjustPrice(ItemStack item, double price)
    {
        double t_price;
        int t_amount;
        if (this.price.get(item)==null)
        {
            t_price = price;
            t_amount = 1;
        }
        else
        {
            t_price = this.price.get(item);
            t_amount = this.amount.get(item);
            t_price *= t_amount;
            t_price += price;
            t_price /= ++t_amount;
        }
        
        AuctionHouse.getInstance().getDB().execUpdate(
            "DELETE FROM `price` WHERE `item`=?", Util.convertItem(item));
        AuctionHouse.getInstance().getDB().execUpdate(
            "INSERT INTO `price` (`item` ,`price` ,`amount` ) VALUES ( ?, ? );", Util.convertItem(item), t_price, t_amount);
        return this.setPrice(item, t_price, t_amount);
    }
    
    public double setPrice(ItemStack item, double price, int amount)
    {
        this.price.remove(item);
        this.price.put(item, price);
        this.amount.remove(item);
        this.amount.put(item, amount);
        return this.price.get(item); 
    }
    
    public void resetPrice(ItemStack item)
    {
        this.price.remove(item);
    }
}
