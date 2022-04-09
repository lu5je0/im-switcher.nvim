local M = {}

local im_server_bin = [[C:\Users\Public\im-switcher\WinImSwitcherServer.jar]]

local function start_server()
  local im_server_bin_local = vim.fn.expand('<sfile>:p:h') .. '/jar/WinImSwitcherServer.jar'
  if vim.fn.filereadable('/mnt/c/Users/Public/im-switcher/WinImSwitcherServer.jar') == 0 then
    vim.fn.system("mkdir -p /mnt/c/Users/Public/im-switcher && cp " .. im_server_bin_local .. " /mnt/c/Users/Public/im-switcher/WinImSwitcherServer.jar")
  end
  vim.fn.jobstart({'java.exe', '-Xmx50m', '-jar', im_server_bin})
end

local function wsl_version()
  if vim.fn.has('wsl') == 0 then
    return 0
  end

  if vim.g.isWsl ~= nil then
    return vim.g.isWsl
  end

  local lines = vim.fn.readfile('/proc/version')
  if string.find(lines[1], 'WSL2') then
    vim.g.isWsl = 2
    return 2
  else
    vim.g.isWsl = 1
    return 1
  end
end

local function wsl_host()
  if wsl_version() == 1 then
    return '127.0.0.1'
  else
    for _, v in ipairs(vim.fn.readfile('/mnt/wsl/resolv.conf')) do
      local _, _, r = string.find(v, '^nameserver (.*)')
      if r ~= nil then
        return r
      end
    end
  end
end

local function init()
  local host = wsl_host() .. ':38714'
  if _G.im_chan_id == nil then
    _G.im_chan_id = vim.fn.sockconnect('tcp', host, { rpc = false, on_data = 'Receive' })
  end

  local function switch_normal_mode(chan_id)
    vim.fn.chansend(chan_id, 'n\n')
  end

  local function switch_insert_mode(chan_id)
    vim.fn.chansend(chan_id, 'i\n')
  end

  local group = vim.api.nvim_create_augroup('packer_reload_augroup', { clear = true })
  vim.api.nvim_create_autocmd('InsertLeave', {
    group = group,
    pattern = { '*' },
    callback = function()
      switch_normal_mode(_G.im_chan_id)
    end,
  })

  vim.api.nvim_create_autocmd('InsertEnter', {
    group = group,
    pattern = { '*' },
    callback = function()
      switch_insert_mode(_G.im_chan_id)
    end,
  })
end

M.setup = function(t)
  start_server()
  init()
end

return M
