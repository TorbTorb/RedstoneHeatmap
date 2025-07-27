# Heatmap

**A minecraft clientside mod for tracking amount of redstone updates and generating a heatmap based on that.**

## Usage:
`/heatmap [subcommand]`

| Subcommand           | Effect                                                                                                                                                                                                         |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| pos1 / pos2          | Sets Corner Position 1 / 2.                                                                                                                                                                                    |
| start \<tick_count>  | Starts recording amount of updates in the cuboid defined by the corner points. If the tick count is set then record data for that amount of ticks and pause afterwards, if it isn't set then run indefinitely. |
| stop                 | Stop recording, this also deletes the recorded data, so make sure to use pause, if you don't want to lose that data.                                                                                           |
| resume \<tick_count> | Resume paused recording for amount of ticks or indefinitely.                                                                                                                                                   |
| pause                | Pause recording.                                                                                                                                                                                               |
| render \<image_size> | Create a top down heatmap image of the recorded area. Default image size is 1024.                                                                                                                              |
| stats                | Print some stats about the recorded Data to the chat.                                                                                                                                                          |
| show                 | Enable in-game rendering.                                                                                                                                                                                      |
| hide                 | Disable in-game rendering.                                                                                                                                                                                     

## Example Heatmap of a simple CPU:

<img width="835" height="1024" alt="heatmap" src="https://github.com/user-attachments/assets/f8c8c69d-eb61-42f2-8afe-89afb4e84d59" />
