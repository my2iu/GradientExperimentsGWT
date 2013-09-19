package org.programmingbasics.my2iu.gradients.shaders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.resources.client.ClientBundle.Source;

public interface GLShaders extends ClientBundle
{
  public static final GLShaders INSTANCE = GWT.create(GLShaders.class);
  
  @Source("vertex_2dpassthrough.shader")
  TextResource vertexPassthrough();

  @Source("vertex_2dpassthrough_wtextures.shader")
  TextResource vertexPassthroughWithTexture();

  @Source("vertex_2dpassthroughcolours.shader")
  TextResource vertexPassthroughWithColours();

  @Source("fragment_white.shader")
  TextResource fragmentWhite();

  @Source("fragment_color.shader")
  TextResource fragmentColor();

  @Source("fragment_texture.shader")
  TextResource fragmentTexture();

  @Source("fragment_gradient.shader")
  TextResource fragmentGradient();

}
