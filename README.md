### Set
Basic Use:
- run with "java -ea -cp bin memory.ServerMain 8080 4", where 8008 is the port and 4 is the number of attributes on a card (only supports 4 right now)
- /look/player route to see the current board
- /declare/player for a player to claim they have a set, giving them rights to pick cards
- /pick/player/row,col for a player to pick a card on the board (zero-indexed)
- /add/player to add 3 cards to the board
- /scores to see current scores

Rules: 
- first person to click a card blocks other players until they have clicked 3 cards, player has a certain time limit to complete their set (something short, like 3sec?)
- some sort of penalty if the 3 cards are not a set, to discourage random clicking
- mechanism for adding 3 cards if nobody can find a set, perhaps require everyone to vote to agree (consider: odds of there not being a set in 15 cards? 18 cards? 21 cards? and adjusting game/UI accordingly), also some sort of time limit so someone can't say no forever

Thoughts:
- implementing some sort of hint mechanism? (if people can't find a Set)
- scoreboard, scoring mechanism? (should there be any time element involved?)
- customizable shapes/colors/etc?
- stuff shamelessly stolen from Jenna on the cpw discord: https://www.maa.org/sites/default/files/pdf/pubs/SetsPlanetsAndComets.pdf, apparently any group of 12 cards is guaranteed a planet, comet, or set? :O also multidimensional set -- variant where you duplicate all cards 3 times and add a border color as a 5th distinguishing characteristic, could be fun
- shuffling two decks together maybe?
- some slides from high school math teacher haha https://drive.google.com/open?id=0B0RzSSfFNbOHflRGYTZ2VkZIZFo4VmhidWFlS2ZYdkRYQS1ZcEk3WmMxdmxpSGxHWDJNdUk
