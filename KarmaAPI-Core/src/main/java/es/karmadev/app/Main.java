package es.karmadev.app;

import es.karmadev.api.file.serializer.FileSerializer;
import es.karmadev.api.file.serializer.SerializeCompressor;
import es.karmadev.api.object.ObjectUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(final String[] args) {
        if (args.length == 0) {
            System.out.println("Please specify a serialize directory");
            return;
        }

        String dirName = "";
        for (String argument : args) {
            if (!argument.startsWith("-")) {
                dirName = argument;
                break;
            }
        }

        if (ObjectUtils.isNullOrEmpty(dirName)) {
            System.out.println("Please specify a serialize directory");
            return;
        }

        Path dir = Paths.get(dirName);
        FileSerializer serializer = new FileSerializer(dir);
        serializer.serialize("gta_iv", SerializeCompressor.LZ4).onComplete((task) -> {
            if (task.error() != null) task.error().printStackTrace();
            System.out.println("Serialized at " + task.get());
        });
    }
}
