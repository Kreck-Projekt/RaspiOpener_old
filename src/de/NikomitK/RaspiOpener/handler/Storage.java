package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import yapion.annotations.object.YAPIONData;
import yapion.hierarchy.output.FileOutput;
import yapion.serializing.YAPIONSerializer;

import java.util.ArrayList;
import java.util.List;

@YAPIONData
@Getter
@Setter
@ToString
public class Storage {

    private String key = null; // will be removed when the new encryption is implemented
    private String hash = null;
    private String nonce = null; // lord forgive me for what I did back then
    private List<String> otps = new ArrayList<>();

    public void save(){
        try{
            Main.getLogger().debug("Storage saved");
            YAPIONSerializer.serialize(this).toYAPION(new FileOutput(Main.getStorageFile(), true)).close();
        } catch (Exception e){
            //TODO dings äh
        }
    }

}
