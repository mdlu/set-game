## Set
#### Basic Use:
- run with "java -ea -cp bin memory.ServerMain 8080 4", where 8008 is the port and 4 is the number of attributes on a card (supports either 3 or 4 right now)
- /look/player route to see the current board
- /declare/player for a player to claim they have a set, giving them rights to pick cards
- /pick/player/row,col for a player to pick a card on the board (zero-indexed)
- /add/player to add 3 cards to the board
- /scores to see current scores
- /watch blocks until a new player joins, a card is clicked, cards are removed or replaced, cards are added, or someone declares a set

#### Card:
A card in Set has four basic attributes:
- color: red, green, purple
- number: one, two, three
- shading: solid, striped, open
- shape: diamond, squiggle, oval
If 4 attributes are desired, all are used; if only 3 are desired, all shapes default to squiggle.

#### Rules: 
- If 4 attributes are being used, a 3x4 board is laid out; if 3 attributes, then a 3x3 board is used.
- The objective is to find Sets, where a Set is defined as three cards where, for each basic attribute, the properties of the three cards are all the same, or all different.
- To declare you have a found a Set, you click "Declare". You then have 5 seconds to click 3 cards which you believe are a Set.
- If your 3 cards make a Set, you gain 10 points; if they are not a Set, or you time out, you lose 5 points.
- If a Set is found, and the board was at its default size (3x4 or 3x3) or smaller, those 3 cards are replaced from the remainder of deck, if any cards remain. Otherwise (if the board was larger than its default size), the 3 cards are removed, and the board is rearranged so it now contains one less column.
- If you believe no Set exists on the given board, you can vote to add 3 more cards by clicking "Add". If all players vote to add, a new column of 3 cards will be added to the board.

#### Thoughts:
- mechanism for adding 3 cards if nobody can find a set, perhaps require everyone to vote to agree (consider: odds of there not being a set in 15 cards? 18 cards? 21 cards? and adjusting game/UI accordingly), also some sort of time limit so someone can't say no forever
- implementing some sort of hint mechanism? (if people can't find a Set)
- scoreboard, scoring mechanism? (should there be any time element involved?)
- customizable shapes/colors/etc?
- stuff shamelessly stolen from Jenna on the cpw discord: https://www.maa.org/sites/default/files/pdf/pubs/SetsPlanetsAndComets.pdf, apparently any group of 12 cards is guaranteed a planet, comet, or set? :O also multidimensional set -- variant where you duplicate all cards 3 times and add a border color as a 5th distinguishing characteristic, could be fun
- shuffling two decks together maybe?
- some slides from high school math teacher haha https://drive.google.com/open?id=0B0RzSSfFNbOHflRGYTZ2VkZIZFo4VmhidWFlS2ZYdkRYQS1ZcEk3WmMxdmxpSGxHWDJNdUk

### Playing on the web
```
REQUEST ::= "/look/" PLAYER
          | "/declare/" PLAYER
          | "/pick/" PLAYER "/" ROW "," COLUMN
          | "/add/" PLAYER
          | "/scores"
          | "/watch/" PLAYER

RESPONSE ::= BOARD | SCORES
BOARD ::= ROW "x" COLUMN NEWLINE DECLARE NEWLINE (SPOT NEWLINE)+
SCORES ::= (PLAYER " " INT " " VOTE NEWLINE)*

PLAYER ::= [\w]+
DECLARE ::= "none" | "up " MILLIS | "my " MILLIS
SPOT ::= "none" | "up " CARD | "my " CARD
MILLIS ::= INT
CARD ::= [^\s\n\r]+
ROW ::= INT
COLUMN ::= INT
INT ::= [0-9]+
VOTE ::= "none" | "add"
NEWLINE ::= "\n" | "\r" "\n"?
```

For `/look/...`, `/declare/...`, `/pick/...`, and `/watch/...` requests, the server responds with `BOARD`, the current board. In the response, `ROW` is the number of rows, `COLUMN` is the number of columns, `DECLARE` is the declare state, and the cards are listed reading across each row, starting with the top row.

`none` indicates no player is declaring a set, `up` indicates another player is declaring a set, and `my` indicates a set is being declared by the player who sent the request. `MILLIS` gives, in Unix time, the time stamp at which the declare will time out.

`none` indicates no card in that location, `up` indicates a face-up card controlled by another player (or by no one), and `my` is a face-up card controlled by the player who sent the request.

For `/scores` and `/add/...` requests, the server responds with `SCORES`, the current scores. In the response, each `PLAYER` is a unique player ID and `INT` is their nonnegative score and `VOTE` is their vote state. `none` indicates the player has not voted and `add` indicates the player votes to add cards to the board.