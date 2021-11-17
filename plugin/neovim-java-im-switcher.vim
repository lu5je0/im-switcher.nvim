" Initialize the channel
if !exists('s:neovimJavaJobId')
  let s:neovimJavaJobId = 0
endif

if has("gui")
    finish
endif

let s:lib = expand('<sfile>:p:h').'/../lib/libinput-source-switcher.dylib'
let s:bin = expand('<sfile>:p:h').'/../jar/im-switcher.jar'
let s:im_server_bin_local = expand('<sfile>:p:h').'/../jar/WinImSwitcherServer.jar'
let s:im_server_bin = 'C:\Users\Public\im-switcher\WinImSwitcherServer.jar'

if !filereadable('/mnt/c/Users/Public/im-switcher/WinImSwitcherServer.jar')
    call system("mkdir -p /mnt/c/Users/Public/im-switcher && cp " .. s:im_server_bin_local .. " /mnt/c/Users/Public/im-switcher/WinImSwitcherServer.jar")
endif

function! s:wsl_version()
    if !has('wsl')
        return 0
    endif

    if exists("g:isWsl")
        return g:isWsl
    endif

    let lines = readfile("/proc/version")
    if lines[0] =~ "WSL2"
        let g:isWsl = 2
        return 2
    else
        let g:isWsl = 1
        return 1
    endif

    let g:isWsl = 0
    return 0
endfunction

if s:wsl_version() == 1
    let s:host = '127.0.0.1'
else
    " let s:host = system("cat /etc/resolv.conf | grep nameserver | awk '{ printf $2 }'")
    let s:host = system("cat /mnt/wsl/resolv.conf | grep nameserver | awk '{ printf $2 }'")
endif

" Entry point. Initialize RPC
function! s:connect()
  let id = s:initRpc()
  
  if 0 == id
    echoerr "neovim-java-im-switcher: cannot start rpc process"
  elseif -1 == id
    echoerr "neovim-java-im-switcher: rpc process is not executable"
  else
    " Mutate our jobId variable to hold the channel ID
    let s:neovimJavaJobId = id 
  endif
endfunction

" Prints logs from java process error stream
function! Receive(job_id, data, event)
  echom printf('%s: %s',a:event,string(a:data))
endfunction


" Initialize RPC
function! s:initRpc()
  if s:neovimJavaJobId == 0
    " let jobid = jobstart(['java', '-Xmx50m', '-jar', s:bin, s:lib], { 'rpc': v:true, 'on_stderr': 'Receive' })
    call jobstart(['java.exe', '-Xmx50m', '-jar', s:im_server_bin])
    let jobid = jobstart(['java', '-Xmx50m', '-jar', s:bin, s:lib, s:host], { 'rpc': v:true})
    return jobid
  else
    return s:neovimJavaJobId
  endif
endfunction

call s:connect()

function! SwitchInsertMode()
    call rpcrequest(s:neovimJavaJobId, 'switchInsertMode')
endfunction

function! SwitchNormalMode()
    call rpcrequest(s:neovimJavaJobId, 'switchNormalMode')
endfunction

augroup switch_im
    autocmd!
    autocmd InsertEnter * call SwitchInsertMode()
    autocmd InsertLeave * call SwitchNormalMode()
augroup END
