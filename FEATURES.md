# Pomelo's Shop Mod - Features

## Core Features

### Graphical Shop Interface
- Browse items across categories with an intuitive GUI
- Visual category navigation with parent-child hierarchy
- Scrollable category list with up/down navigation buttons
- Item grid display with hover tooltips
- Price display overlay on items (buy/sell prices)

### Player Trading System
- Players can list their own items for sale
- Set both buy and sell prices for each item
- Support for bulk item listing with quantity settings
- Item sellers are tracked and displayed

### Currency System
- Built-in per-player money system
- Persistent storage in player data
- Admin commands to add/set player balance
- Balance check command for players

### Key Binding
- Quick shop access with the "N" key (configurable)
- Customizable in Controls settings

### Category Management
- Create categories with custom ID and display name
- Delete categories with confirmation dialog
- Parent-child category hierarchy support
- Move categories up/down in order
- Set category parent via command
- Right-click to edit category name
- Sub-categories display indented under parent categories

### Item Banning
- Admins can ban specific items from being listed
- Unban previously banned items
- View list of all banned items
- Prevents banned items from being added to shop

### Configurable Permissions
- Toggle which actions require admin privileges
- Permission level 2 required for admin commands

### Bilingual Support
- Full Chinese (zh_cn) and English (en_us) translations
- All GUI elements, commands, and messages translated

### Auto-Save
- Automatic data persistence every 5 seconds
- Shop data saved to world directory
- Reliable data recovery on server restart

---

## Advanced Features

### Stock Management System
- Set stock quantity for each item
- Stock states:
  - `stock = 0` — Unlimited stock (default)
  - `stock > 0` — Limited stock with count
  - `stock = -1` — Sold out (cannot purchase)
- Stock display on items:
  - Sold out items show red "Sold Out" text
  - Limited stock items show quantity (e.g., "x10")
  - Tooltip shows stock info below item name
- Purchase restrictions:
  - Cannot buy sold out items
  - Cannot buy more than available stock
  - Stock decreases after purchase
- Sell adds stock:
  - Selling items increases stock by sold quantity
  - Sold out items become available again after selling
- Edit stock via item edit screen

### Item Copy & Paste
- Copy item info with Ctrl+C while hovering
- Paste item to current category with Ctrl+V
- Pasted items added at the end of category
- Copies all item properties: item, prices, stock

### Category Delete Confirmation
- Left-click delete shows confirmation dialog
- Displays category name to be deleted
- Confirm and Cancel buttons
- Prevents accidental deletion

### Edit Item Enhancements
- Change item via inventory selection screen
- Edit buy price, sell price, category, stock
- Replace item with different item from inventory
- All changes sync to all online players

### Shop Data Import/Export
- Export shop data to JSON: `/shop export <filename>`
- Import shop data from JSON: `/shop import <filename>`
- Exported files saved in `/shop_exports/<filename>.json`
- Full backup and restore capability

### Category Sorting
- Move category up: `/shop category moveup <name>`
- Move category down: `/shop category movedown <name>`
- Set category parent: `/shop category setparent <child> <parent>`
- Remove category parent: `/shop category setparent <child> none`
- Supports Chinese category names with quotes

---

## Shop Interface Controls

| Action | Description |
|--------|-------------|
| Left Click Item | Buy item (cannot buy if stock is 0 or sold out) |
| Right Click Item | Sell 1 item (stock +1 after selling) |
| Shift + Right Click Item | Batch sell all items in inventory (stock increases by amount) |
| Ctrl + Right Click Item | Delete your own listing |
| Shift + Left Click Item | Edit your own listing (price, category, stock, change item) |
| Ctrl + C (hover on item) | Copy item information |
| Ctrl + V | Paste copied item to current category |

---

## Admin Commands

| Command | Description |
|---------|-------------|
| `/shop add <player> <amount>` | Add money to player's balance |
| `/shop set <player> <amount>` | Set player's balance |
| `/shop reload` | Reload shop data from storage |
| `/shop clear` | Clear all shop items |
| `/shop category moveup <name>` | Move category up in order |
| `/shop category movedown <name>` | Move category down in order |
| `/shop category setparent <child> <parent>` | Set category's parent |
| `/shop category setparent <child> none` | Remove category's parent |
| `/shop ban <item_id>` | Ban item from being listed |
| `/shop unban <item_id>` | Unban a banned item |
| `/shop banlist` | View banned items list |
| `/shop export <filename>` | Export shop data to JSON |
| `/shop import <filename>` | Import shop data from JSON |

---

## Player Commands

| Command | Description |
|---------|-------------|
| `/shop` | Open shop interface |
| `/shop balance` | Check current balance |
| `/shop sell <price> [amount]` | List item in hand for sale |
| `/shop category create <ID> <name>` | Create a new category |
| `/shop category list` | List all categories |

---

## Technical Details

### Data Storage
- Shop items stored in `world/pomeloshopmod/shop_items.json`
- Categories stored in `world/pomeloshopmod/categories.json`
- Player money stored in player NBT data
- Exported files in `shop_exports/` directory

### Network Synchronization
- Real-time sync of shop items to all players
- Category changes broadcast to all clients
- Stock updates sync immediately after purchase/sell

### Performance Optimizations
- Cached item hover names to reduce overhead
- Efficient category hierarchy caching
- Batch network packets for bulk operations

---

## Future Planned Features

- Auction system with bidding
- Shop search functionality
- Transaction history log
- Discount/promotion system
- Multi-shop support (different shops per region)
- Trading permissions per category