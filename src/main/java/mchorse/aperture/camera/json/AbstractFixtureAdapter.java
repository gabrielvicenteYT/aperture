package mchorse.aperture.camera.json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.modifiers.ShakeModifier;

/**
 * This class is responsible for serializing and deserializing an abstract
 * camera fixtures types.
 */
public class AbstractFixtureAdapter implements JsonSerializer<AbstractFixture>, JsonDeserializer<AbstractFixture>
{
    /**
     * Gson instance for building up
     */
    private Gson gson;

    /**
     * Hash map of fixtures types mapped to corresponding class. I think this
     * code should be moved to the AbstractFixture class.
     */
    public static final Map<String, Class<? extends AbstractFixture>> TYPES = new HashMap<String, Class<? extends AbstractFixture>>();

    static
    {
        TYPES.put("idle", IdleFixture.class);
        TYPES.put("path", PathFixture.class);
        TYPES.put("look", LookFixture.class);
        TYPES.put("follow", FollowFixture.class);
        TYPES.put("circular", CircularFixture.class);
    }

    public AbstractFixtureAdapter()
    {
        GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        AbstractModifierAdapter modifierAdapter = new AbstractModifierAdapter();

        builder.registerTypeAdapter(AbstractModifier.class, modifierAdapter);
        builder.registerTypeAdapter(ShakeModifier.class, modifierAdapter);

        this.gson = builder.create();
    }

    /**
     * Deserialize an abstract fixture from JsonElement.
     *
     * This method extracts type from the JSON map, and creates a fixture
     * from the type (which is mapped to a class). It also responsible for
     * setting the target for follow and look fixtures.
     */
    @Override
    public AbstractFixture deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        String type = object.get("type").getAsString();
        AbstractFixture fixture = this.gson.fromJson(json, TYPES.get(type));

        fixture.fromJSON(object);

        return fixture;
    }

    /**
     * Serialize an abstract fixture into JsonElement.
     *
     * This method also responsible for giving the serialized abstract fixture
     * a type key (for later ability to deserialize exact type of the JSON
     * element) and target key for look and follow fixtures.
     */
    @Override
    public JsonElement serialize(AbstractFixture src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = (JsonObject) this.gson.toJsonTree(src);

        src.toJSON(object);
        object.addProperty("type", getKeyByValue(TYPES, src.getClass()));

        return object;
    }

    /**
     * From StackOverflow
     */
    public static <T, E> T getKeyByValue(Map<T, E> map, E value)
    {
        for (Entry<T, E> entry : map.entrySet())
        {
            if (Objects.equal(value, entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return null;
    }
}