package com.megatron.test.jme3;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.geomipmap.grid.*;
import com.jme3.terrain.geomipmap.lodcalc.*;
import com.jme3.terrain.noise.*;
import com.jme3.terrain.noise.basis.*;
import com.jme3.terrain.noise.filter.*;
import com.jme3.terrain.noise.fractal.*;
import com.jme3.terrain.noise.modulator.*;
import com.jme3.texture.*;
import com.jme3.texture.Texture.WrapMode;
import com.megatron.model.City;

public class HelloJME3 extends SimpleApplication {

    private TerrainQuad terrain;
    private Material mat;
    private float grassScale = 64;
    private float dirtScale = 16;
    private float rockScale = 128;
    private FractalSum base;
    private PerturbFilter perturb;
    private OptimizedErode therm;
    private SmoothFilter smooth;
    private IterativeFilter iterate;

    public HelloJME3() {
        setShowSettings(false);
        setDisplayStatView(false);
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1024);
        settings.setHeight(768);
        setSettings(settings);
    }

    public static void main(String[] args) {
        HelloJME3 app = new HelloJME3();
        app.start();
    }

    @Override
    public void simpleInitApp() {

    	City city = new City();
    	
        this.flyCam.setMoveSpeed(100f);
        ScreenshotAppState state = new ScreenshotAppState();
        this.stateManager.attach(state);

        // TERRAIN TEXTURE material
        mat = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

        // DIRT texture
        Texture dirt = this.assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        // GRASS texture
        Texture grass = this.assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        // ROCK texture
        Texture rock = this.assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
        rock.setWrap(WrapMode.Repeat);

        this.mat.setTexture("region1ColorMap", grass);
        this.mat.setVector3("region1", new Vector3f(15, 200, this.grassScale));

        this.mat.setTexture("region2ColorMap", dirt);
        this.mat.setVector3("region2", new Vector3f(0, 20, this.dirtScale));

        this.mat.setTexture("region3ColorMap", rock);
        this.mat.setVector3("region3", new Vector3f(198, 260, this.rockScale));

        this.mat.setTexture("region4ColorMap", rock);
        this.mat.setVector3("region4", new Vector3f(198, 260, this.rockScale));

        this.mat.setTexture("slopeColorMap", rock);
        this.mat.setFloat("slopeTileFactor", 32);

        this.mat.setFloat("terrainSize", city.getSize() + 1);

        this.mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        // this.mat.getAdditionalRenderState().setWireframe(true);

        this.base = new FractalSum();
        this.base.setRoughness(0.7f);
        this.base.setFrequency(1.0f);
        this.base.setAmplitude(1.0f);
        this.base.setLacunarity(2.12f);
        this.base.setOctaves(8);
        this.base.setScale(0.02125f);
        this.base.addModulator(new NoiseModulator() {

            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });

        FilteredBasis ground = new FilteredBasis(this.base);

        this.perturb = new PerturbFilter();
        this.perturb.setMagnitude(0.119f);

        this.therm = new OptimizedErode();
        this.therm.setRadius(5);
        this.therm.setTalus(0.011f);

        this.smooth = new SmoothFilter();
        this.smooth.setRadius(1);
        this.smooth.setEffect(0.7f);

        this.iterate = new IterativeFilter();
        this.iterate.addPreFilter(this.perturb);
        this.iterate.addPostFilter(this.smooth);
        this.iterate.setFilter(this.therm);
        this.iterate.setIterations(1);

        ground.addPreFilter(this.iterate);

        FractalTileLoader ftl = new FractalTileLoader(ground, 256f);
        ftl.setPatchSize(65);
        ftl.setQuadSize(city.getSize()+1);
        this.terrain = ftl.getTerrainQuadAt(new Vector3f(0f,0f,0f));
        this.terrain.setMaterial(this.mat);
        this.terrain.setLocalTranslation(0, 0, 0);
        this.terrain.setLocalScale(2f, 1f, 2f);
        this.rootNode.attachChild(this.terrain);
        // ..
        TerrainLodControl control = new TerrainLodControl(this.terrain, this.getCamera());
        control.setLodCalculator(new DistanceLodCalculator(33, 2.7f)); // patch
                                                                       // size,
                                                                       // and a
                                                                       // multiplier
        this.terrain.addControl(control);

        this.getCamera().setLocation(new Vector3f(0, 300, 0));

        this.viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

    }
}