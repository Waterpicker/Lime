#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(push_constant) uniform PushConstant {
    int[] constants;
    //0 = element.id which is the model index in the model list
} pc;

layout(binding = 0) uniform UniformBufferObject {
    mat4[10] model;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec3 inColor;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out vec3 fragColor;
layout(location = 1) out vec2 fragTexCoord;

void main() {
    gl_Position = ubo.model[pc.constants[0]] * vec4(inPosition, 0.0, 1.0);
    fragColor = inColor;
    fragTexCoord = vec2((fragTexCoord.x+1.0)/2.0, 1 - (fragTexCoord.y+1.0)/2.0); //flips the uv's i think
}
