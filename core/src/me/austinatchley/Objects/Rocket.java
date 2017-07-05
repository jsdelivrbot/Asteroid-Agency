package me.austinatchley.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.sun.org.apache.bcel.internal.generic.CALOAD;

import javax.xml.bind.util.ValidationEventCollector;

import me.austinatchley.States.State;

public class Rocket extends SpaceObject {
    private static final int VERTICAL_OFF = 20;
    private static final float DEG2RAD = MathUtils.degreesToRadians;
    private static final float CHANGE = 10f * DEG2RAD;

    public ParticleEffect thruster;

    public Rocket(World world){
        super(world);
        image = new Texture("outline.png");
        sprite = new Sprite(image);

        thruster = new ParticleEffect();
        thruster.load(Gdx.files.internal("rocket_thruster.p"), Gdx.files.internal(""));

        init();

        thruster.start();
        thruster.setPosition(body.getPosition().x, body.getPosition().y);
        thruster.getEmitters().first().getAngle().setLow(body.getAngle());
        thruster.getEmitters().first().getAngle().setHigh(body.getAngle());
    }

    public void init() {
        BodyDef rocketBodyDef = new BodyDef();
        rocketBodyDef.type = BodyDef.BodyType.KinematicBody;
        rocketBodyDef.position.set((State.WIDTH - image.getWidth())/ 2, VERTICAL_OFF);

        body = world.createBody(rocketBodyDef);

        MassData rocketMassData = new MassData();
        rocketMassData.mass = 10f;
        body.setMassData(rocketMassData);
        body.setUserData("Rocket");

        PolygonShape rocketShape = new PolygonShape();
        rocketShape.setAsBox(image.getWidth()/2, image.getHeight());

        FixtureDef rocketFixtureDef = new FixtureDef();
        rocketFixtureDef.shape = rocketShape;

        Fixture rocketFixture = body.createFixture(rocketFixtureDef);
        rocketFixture.setUserData("Rocket");
        rocketShape.dispose();
    }

    public void render(SpriteBatch batch){
        float posX = body.getPosition().x;
        float posY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        sprite.setPosition(posX, posY);
        sprite.setRotation(rotation);

        // Then we simply draw it as a normal sprite.
        sprite.draw(batch);

        thruster.setPosition(body.getPosition().x, body.getPosition().y);
        for (ParticleEmitter emitter :  thruster.getEmitters()) { //get the list of emitters - things that emit particles
            emitter.getAngle().setLow(rotation / DEG2RAD); //low is the minimum rotation
            emitter.getAngle().setHigh(rotation / DEG2RAD); //high is the max rotation
        }
        thruster.draw(batch, Gdx.graphics.getDeltaTime());
    }

    public void rotateTowards(Vector2 target){
        Vector2 toTarget = new Vector2(target.x - body.getPosition().x,
                target.y - body.getPosition().y);

        float desiredAngle = MathUtils.atan2(-toTarget.x, toTarget.y);
        float totalRotation =  desiredAngle - body.getAngle();
        while ( totalRotation < -180 * DEG2RAD ) totalRotation += 360 * DEG2RAD;
        while ( totalRotation >  180 * DEG2RAD ) totalRotation -= 360 * DEG2RAD;
        float newAngle = body.getAngle() + Math.min(CHANGE, Math.max(-CHANGE, totalRotation));

        setTransform(body.getPosition(), newAngle);
    }

    public void moveTo(Vector2 target) {
        //rotateTowards(target);
        setTransform(target, body.getAngle());
    }

    public void fireLaser(){

    }
}