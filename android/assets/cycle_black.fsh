#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord0;

uniform vec4 u_color;
uniform sampler2D u_sampler2D;

void main() {
	vec4 color = texture2D(u_sampler2D, v_texCoord0) * v_color;
	if (color.rgb == vec3(0,0,0)) {
	    gl_FragColor = u_color;
	} else {
	    gl_FragColor = color;
	}
}
