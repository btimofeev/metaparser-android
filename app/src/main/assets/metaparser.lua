local std = stead

local iface = std.ref "@iface"
local instead = std.ref "@instead"

local function html_tag(nam)
	return function(s, str)
		if not str then return str end
		return '<'..nam..'>'..str..'</'..nam..'>'
	end
end

iface.bold = html_tag('b')
iface.em = html_tag('i')
iface.under = html_tag('u')
iface.st = html_tag('st')
iface.center = html_tag('center')
iface.right = html_tag('right')

instead.restart = instead_restart
instead.menu = instead_menu
instead.tiny = true

std.mod_start(function()
	local mp = std.ref '@metaparser'
	if mp then
		mp.msg.CUTSCENE_HELP = "Для продолжения нажмите {$fmt b|ввод} или введите {$fmt em|дальше}."
		mp.msg.CUTSCENE_MORE = "^Для продолжения нажмите {$fmt b|ввод} или введите {$fmt em|дальше}."
		MetaVerb {
        	"#MetaRestart",
        	"~заново,~рестарт,~restart",
        	"MetaRestart"
        }
	end
end)
