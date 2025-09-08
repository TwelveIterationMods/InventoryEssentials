- Hofix: Fixed crash on screens with no real menu behind them

- Added a way to ignore screens, slots or menus by type/class either in a mod or in config folder
    - Place a json file under `config/inventoryessentials/ignores/example.json` with the fields `ignoredScreenClasses`,
      `ignoredMenuClasses`, `ignoredSlotClasses` or `ignoredMenuTypes` (each a string array)
    - Mod developers can also include these files in their JARs (`/inventoryessentials/ignores/yourmod.json` - not inside data or assets!)
- Fixed shift drag not working inside Sophisticated Storage


- Fixed drag transfer activating even on mouse buttons other than left and right click