package me.austinatchley.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.Stack;

import me.austinatchley.Tools.Utils;

public abstract class SpaceObject {
    static final float DEG2RAD = MathUtils.degreesToRadians;

    Body body;
    Texture image;
    Sprite sprite;
    World world;

    long targetTime, lastTime;

    // Buffer of object transforms, with the most recent stored at currentTransformIndex
    int currentTransformIndex;
    Transform[] transformBuffer;

    public SpaceObject(World world) {
        this.world = world;
    }

    public void init() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(Utils.p2m(0, 0));

        body = world.createBody(bodyDef);

        targetTime = System.currentTimeMillis();
        lastTime = 0;
        currentTransformIndex = 0;
        transformBuffer = new Transform[5];

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(image.getWidth(), image.getHeight());

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits = 0x0001;
        fixtureDef.filter.maskBits = 0x0002;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("");
    }

    public void render(SpriteBatch batch) {
        Vector2 pos = getPosition();
        float rotation = body.getAngle() / DEG2RAD;
        sprite.setPosition(pos.x - image.getWidth() / 2f, pos.y - image.getHeight() / 2f);
        sprite.setRotation(rotation);

        // Then we simply draw it as a normal sprite.
        sprite.draw(batch);
    }

    public float getWidth() {
        return image.getWidth();
    }

    public float getHeight() {
        if (image == null) return 0f;
        return image.getHeight();
    }

    public Body getBody() {
        return body;
    }

    /*
    Returns position of SpaceObject in pixels
     */
    public Vector2 getPosition() {
        if (body == null) return null;
        return Utils.m2p(body.getPosition());
    }

    /*
    Returns position of SpaceObject body in meters
     */
    public Vector2 getBodyPosition() {
        if (body == null) return null;
        return body.getPosition();
    }

    public float getAngle() {
        if (body == null) return 0f;
        return body.getAngle();
    }

    public void setTransformLerp(Vector2 pos, float rotation) {
        if (transformBuffer == null) {
            setTransform(pos, rotation);
            return;
        }

        Transform lastTransform = transformBuffer[currentTransformIndex];
        float t = 0f;

        // If we don't have a previous snapshot, just set the transform
        if (lastTransform == null) {
            setTransform(pos, rotation);
            return;
        }

        Vector2 newPos = lastTransform.getPosition().lerp(pos, t);
        float newRotation = Utils.lerp(lastTransform.getRotation(), rotation, t);
        Transform newTransform = new Transform(newPos, newRotation);

        setTransform(newTransform);

        currentTransformIndex = (currentTransformIndex + 1) % transformBuffer.length;
        transformBuffer[currentTransformIndex] = newTransform;
    }

    public void setTransform(Transform transform) {
        setTransform(transform.getPosition(), transform.getRotation());
    }

    public void setTransform(Vector2 pos, float rotation) {
        if (body == null) return;
        body.setTransform(Utils.p2m(pos), rotation);
    }

    public void setTransform(float x, float y, float rotation) {
        setTransform(new Vector2(x, y), rotation);
    }

    public int scoreEffect() {
        return 1;
    }

    public void dispose() {
        image.dispose();
//        world.destroyBody(body);
    }
}
