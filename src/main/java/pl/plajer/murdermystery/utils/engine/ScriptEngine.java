package pl.plajer.murdermystery.utils.engine;

import org.bukkit.Bukkit;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.logging.Level;

public class ScriptEngine {

    private javax.script.ScriptEngine scriptEngine;

    public ScriptEngine() {
        scriptEngine = new ScriptEngineManager().getEngineByName("js");
    }

    public void setValue(String value, Object valueObject) {
        scriptEngine.put(value, valueObject);
    }

    public void execute(String executable) {
        try {
            scriptEngine.eval(executable);
        } catch(ScriptException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Script failed to parse expression! Expression was written wrongly!");
            Bukkit.getLogger().log(Level.SEVERE, "Expression value: " + executable);
            Bukkit.getLogger().log(Level.SEVERE, "Error log:");
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "---- THIS IS AN ISSUE BY USER CONFIGURATION NOT AUTHOR BUG ----");
        }
    }

}