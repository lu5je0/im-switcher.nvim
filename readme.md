## im-switcher.nvim
Switch your input method from wsl or wsl2. The delay of switching between normal and insert modes is only 1ms, which is more than 70 times faster than im-select.exe(70+ms).

## Requirements
Java 11 or higher on WSL and Windows is required.

## Installation
packer.nvim
```lua
use {
'lu5je0/im-switcher',
disable = vim.fn.has("wsl") == 0
}
```
