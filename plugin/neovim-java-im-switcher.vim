" Initialize the channel
if !exists('s:neovimJavaJobId')
  let s:neovimJavaJobId = 0
endif

if has("gui")
    finish
endif

" Path to JAR
let s:lib = expand('<sfile>:p:h').'/../lib/libinput-source-switcher.dylib'
let s:bin = expand('<sfile>:p:h').'/../jar/im-switcher.jar'
let s:im_server_bin = get(g:, 'WinImSwitcherServerPath', 'D:\IdeaProjects\im-switcher\jar\WinImSwitcherServer.jar')

function! s:is_wsl()
    if exists("g:isWsl")
        return g:isWsl
    endif

    if has("unix")
        let lines = readfile("/proc/version")
        if lines[0] =~ "Microsoft"
            let g:isWsl=1
            return 1
        endif
    endif
    let g:isWsl=0
    return 0
endfunction

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
    if s:is_wsl() == 1
        call jobstart(['java.exe', '-Xmx50m', '-jar', s:im_server_bin])
    endif
    let jobid = jobstart(['java', '-Xmx50m', '-jar', s:bin, s:lib], { 'rpc': v:true})
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
