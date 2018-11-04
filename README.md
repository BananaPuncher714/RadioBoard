# RadioBoard
### What is this?
RadioBoard is a Bukkit plugin that uses map items to draw pictures and play gifs. It is comprised of 2 parts, virtual canvases, and board frames

Virtual canvases are just a blank slate upon which images or shapes can be draw on, and displayed to the player. They can be spread across several board frames, or none at all.

Board frames are in-game structures that are made of a wall with an array of item frames with maps inside of them. Board frames can be of any width or height, and several can be made to show the same Virtual Canvas.

### Limitations:
Unfortunately, due to Minecraft's packet limit threshold, it is not possible to send an unlimited amount of map packets at the player without the client timing out. Gifs and animations should be limited to roughly 500 full map packets per second. This does *not* mean that the server cannot send out more than 500 packets, simply that any individual player should not receive more than 500 packets per second.

### Commands:
- video <board|show> ...
  - board <name> <map-id>
    Execute while looking at an item frame to construct a board. The name is unique per board
  - show <name> <map-id> <file-name> <x:y>
    The file has to be located under /plugins/RadioBoard/images/, and the X and Y are the width and height of the maps. The name is arbitrary.

### Contributors:
- BananaPuncer714: Creator, Developer
- Jetp250: Developer