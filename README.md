# __RadioBoard__
![RadioBoard logo](https://i.imgur.com/bCKA5dO.png)
## About
### What is this?
RadioBoard is a Bukkit plugin that uses map items to draw pictures and play GIFs. It is comprised of 2 parts, virtual canvases, and board frames

Virtual canvases are just a blank slate upon which images or shapes can be draw on, and displayed to the player. They can be spread across several board frames, or none at all.

Board frames are in-game structures that are made of a wall with an array of item frames with maps inside of them. Board frames can be of any width or height, and several can be made to show the same Virtual Canvas. Here is an example of one:
![boardframe](https://i.imgur.com/cIj5qWb.png)

Most of RadioBoard configuration is done through other plugins, or configs. RadioBoard will generate 2 folders by default, one for images and another for display providers. The image folder has the path `/plugins/RadioBoard/images` and the provider folder has the path `/plugins/RadioBoard/providers`. These are where you will place resources that you want to include for your boards. Virtual canvases can be added through the `/plugins/RadioBoard/boards.yml` file.

RadioBoard also supports *very* accurate click detection and can detect exactly what pixel has been clicked.

### How does this work?
You can have several BoardFrames positioned around important places, such as the spawn, or your hub. Along with those, you can link displays to show something, such as a welcome message. Only people in the same world and with the proper permissions will be able to see, or interact with them at all. As a bonus, the item frames that make up the board cannot be destroyed normally.
![kingey](https://i.imgur.com/Za1jkqe.png)

### Limitations:
Unfortunately, due to Minecraft's packet limit threshold, it is not possible to send an unlimited amount of map packets at the player without the client timing out. Gifs and animations should be limited to roughly 500 full map packets per second. This does *not* mean that the server cannot send out more than 500 packets, simply that any individual player should not receive more than 500 packets per second. RadioBoard already optimizes the packet size as much as possible, so try not to break it. :)
![gif](https://i.imgur.com/sDYRBcj.gif)

### Versions supported:
RadioBoard supports versions 1.11.2 and 1.12.2 at the moment. I will *NOT* be supporting 1.10 or below due to how outdated they are.

## Usage
RadioBoard comes with a lot of great core features, such as full dynamic interactive boards that support GIFs, images, buttons, switches, and cool special effects. It also includes a full api for developers.

### Commands:
- `radioboard <board|display|list> ...`
  - `board <create|remove> ...`
    - `board create <name> <map-id>`
      Execute while looking at an item frame to construct a board. The name is unique per board
    - `board remove <name>`
      Remove a board with the given name from existence
  - `display <create|remove> ...`
    - `display create <name> <map-id> <file-name> <x:y>`
      Create a new display based off a template which can be found under /plugins/RadioBoard/providers/
    - `display remove <name>`
      Delete a display forever!(A long time!)
  - `list <boards|displays>`
    View detailed information about all registered boards or displays

### Permissions:
- `radioboard.admin`
  Allows users to run all the commands
- `radioboard.board.<board>.view`
  Allows users to view a default board
- `radioboard.board.<board>.interact`
  Allows users to interact with a default board, assuming they can see it

### How to add boards:
The `/plugins/RadioBoard/boards.yml` is where you will be storing your boards. You can also create boards in-game, too. A board follows this format:
```YAML
displays:
  # The board starts here
  # First we need a name for our board
  example_display:
    # Then, we give it an id of a map
    id: 0
    # Here are the height and the width in terms of maps
    width: 6
    height: 6
    # This is the most important part. It tells us where we can find the template for the layout of our display
    provider: "example-canvas.yml"
```
The provider's file can be found under `/plugins/RadioBoard/providers`.

### How to create provider/canvas templates:
Canvas templates are just files that tell us how we want to position what elements on a display. A simple template looks like this:
```YAML
# Height and width in pixels. Remember a map is 128x128 pixels
width: 768
height: 768
# The location of a background image
background-image: "example/nyan_cat_background.jpg"
elements:
  # The type of element we want to make
  - type: "GIF"
    # The X and Y offset from the top left corner; Can be negative
    x: 480
    y: 80
    # The width and height of this element in pixels
    width: 250
    height: 188
    # Each element has different data it requires
    data:
      gif: "example/kurisu.gif"
```
The background image and all images referenced can be found relative to the `/plugins/RadioBoard/images` folder. They may exist in sub folders if you would like to organize them that way as well.

There are currently 5 different types of elements. A BUTTON, CLOUD, IMAGE, SWITCH, and GIF.
- Buttons require a `clicked-image`, `unclicked-image`, `command` and `delay` option. The first two options must be a path to an image. The command can either be run by a player or the console, which can be determined by whether or not a `/` is placed in front. RadioBoard also automatically replaces `%player_name%` with the name of the player who activated it. The delay option is how long the button stays pressed in milliseconds.
- Clouds require a `transparency` and a `delay` option. the transparency option tells how transparent a cloud should be, from 0 to 255 with 255 being completely opaque. The delay option tells how long in milliseconds for a cloud to update its position and send it to the client.
- Images require a `image` option, which is the name of the image file the user wants to display.
- Switches require `on-image`, `off-image`, `on-command`, and `off-command`. The images are what gets displayed when the switch is on or off, and the commands are what gets run when the switch gets flipped. Commands get parsed just like button commands.
- Gifs require a `gif` option, which tells the location of a gif relative to the image folder.

## Tutorials:
For more, check out the Github wiki and repo!

## Credits
### Contributors:
- `BananaPuncer714` Creator, Developer
- `Jetp250` Developer