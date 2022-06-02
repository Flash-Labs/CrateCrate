package dev.flashlabs.cratecrate.internal.converter;

import dev.flashlabs.cratecrate.CrateCrate;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.format.Generator;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

/**
 * This class is the same as {@link dev.willbanders.storm.format.storm.StormGenerator},
 * however also incorporates some heuristics for custom rendering. The resulting
 * config is semantically identical.
 */
public final class ComponentConfigGenerator extends Generator {

    public ComponentConfigGenerator(PrintWriter writer) {
        super(writer);
    }

    @Override
    protected void generateRoot(Node root) {
        CrateCrate.get().getLogger().info("root");
        System.out.println("println");
        generateComment(root);
        if (root.getType() == Node.Type.OBJECT) {
            if (!root.getComment().isEmpty()) {
                newline(indent);
            }
            generateProperties(root);
        } else {
            generateNode(root);
        }
    }

    @Override
    protected void generateComment(Node node) {
        if (!node.getComment().isEmpty()) {
            Arrays.stream(node.getComment().split("\n\r|\r\n|\n|\r")).forEach(c -> {
                write("//", c);
                newline(indent);
            });
        }
    }

    @Override
    protected void generateCharacter(Node node) {
        write("\'", escape(node.getValue().toString()), "\'");
    }

    @Override
    protected void generateString(Node node) {
        write("\"", escape(node.getValue().toString()), "\"");
    }

    private String escape(String string) {
        return string.replace("\\", "\\\\")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\'", "\\'")
            .replace("\"", "\\\"");
    }

    @Override
    protected void generateArray(Node node) {
        write("[");
        if (!node.getList().isEmpty()) {
            //attempt to identity reference values to render inline
            if (node.getParent().getType() == Node.Type.ARRAY && node.getList().stream().allMatch(n ->
                n.getComment().isEmpty() && n.getType() != Node.Type.ARRAY && n.getType() != Node.Type.OBJECT)) {
                for (int i = 0; i < node.getList().size(); i++) {
                    write(node.resolve(i));
                    if (i != node.getList().size() - 1) {
                        write(", ");
                    }
                }
            } else {
                newline(++indent);
                for (int i = 0; i < node.getList().size(); i++) {
                    generateComment(node.resolve(i));
                    write(node.resolve(i));
                    if (i != node.getList().size() - 1) {
                        newline(indent);
                    }
                }
                newline(--indent);
            }
        }
        write("]");
    }

    @Override
    protected void generateObject(Node node) {
        write("{");
        if (!node.getMap().isEmpty()) {
            newline(++indent);
            generateProperties(node);
            newline(--indent);
        }
        write("}");
    }

    private void generateProperties(Node node) {
        CrateCrate.get().getLogger().info("Generate properties: " + node.getPath());
        int i = 0;
        for (Map.Entry<String, Node> entry : node.getMap().entrySet()) {
            generateComment(entry.getValue());
            if (entry.getKey().matches("[A-Za-z_][A-Za-z0-9_-]*")) {
                write(entry.getKey());
            } else {
                write("\"", escape(entry.getKey()) + "\"");
            }
            //attempt to identify colors to render in hex
            CrateCrate.get().getLogger().info(entry.getKey() + ": " + entry.getValue().getType());
            if (entry.getKey().equals("color") && entry.getValue().getType() == Node.Type.INTEGER) {
                CrateCrate.get().getLogger().info("found color");
                write(" = ", String.format("0x%06X", (BigInteger) entry.getValue().getValue()));
            } else if ((entry.getKey().equals("colors") || entry.getKey().equals("fades")) && entry.getValue().getType() == Node.Type.ARRAY) {
                write(" = [");
                for (int j = 0; j < entry.getValue().getList().size(); j++) {
                    if (entry.getValue().resolve(j).getType() == Node.Type.INTEGER) {
                        write(String.format("0x%06X", (BigInteger) entry.getValue().resolve(j).getValue()));
                    } else {
                        write(entry.getValue());
                    }
                    if (i != entry.getValue().getList().size() - 1) {
                        write(", ");
                    }
                }
            } else {
                write(" = ", entry.getValue());
            }
            if (i++ != node.getMap().size() - 1) {
                newline(indent);
            }
        }
    }

}
