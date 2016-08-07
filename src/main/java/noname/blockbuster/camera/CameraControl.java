package noname.blockbuster.camera;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import noname.blockbuster.camera.fixtures.AbstractFixture;
import noname.blockbuster.camera.fixtures.PathFixture;
import noname.blockbuster.network.Dispatcher;
import noname.blockbuster.network.common.PacketLoadCameraProfile;

/**
 * Camera control class
 *
 * This class is responsible for controlling the camera profile. The actions
 * are delegated from keyboard handler.
 */
public class CameraControl
{
    public int index = 0;
    public CameraProfile profile;

    /**
     * Get currently selected camera fixture
     */
    public AbstractFixture current() throws CommandException
    {
        if (this.profile.has(this.index))
        {
            return this.profile.get(this.index);
        }

        throw new CommandException("blockbuster.profile.not_exists", this.index);
    }

    /**
     * Add passed fixture to given camera profile. In case of error this
     * method would inform the player about error.
     */
    public void add(EntityPlayer player, AbstractFixture fixture) throws CommandException
    {
        fixture.edit(new String[] {}, player);
        this.profile.add(fixture);
    }

    /**
     * Edit currently selected camera fixture
     */
    public void edit(EntityPlayer player) throws CommandException
    {
        this.current().edit(new String[] {}, player);
    }

    /**
     * Remove current selected fixture
     */
    public void remove()
    {
        if (!this.profile.has(this.index)) return;

        this.profile.remove(this.index);
        this.index--;
    }

    /**
     * Add a point to currently bound path fixture
     */
    public void addPoint(Position pos) throws CommandException
    {
        PathFixture path = this.path();

        if (path != null)
        {
            path.addPoint(pos);
        }
    }

    /**
     * Remove point from currently bound path fixture
     */
    public void removePoint() throws CommandException
    {
        PathFixture path = this.path();

        if (path != null)
        {
            int size = path.getPoints().size();

            if (size > 1)
            {
                path.removePoint(size - 1);
            }
        }
    }

    /**
     * Get current path fixture
     * @throws CommandException
     */
    protected PathFixture path() throws CommandException
    {
        AbstractFixture fixture = this.current();

        if (fixture instanceof PathFixture)
        {
            return (PathFixture) fixture;
        }

        throw new CommandException("blockbuster.profile.not_path", this.index);
    }

    /**
     * Add or reduce the duration of currently selected camera fixture
     * @throws CommandException
     */
    public void addDuration(long add) throws CommandException
    {
        AbstractFixture fixture = this.current();
        long duration = fixture.getDuration() + add;

        if (duration > 0)
        {
            fixture.setDuration(duration);
        }
    }

    public void next()
    {
        this.index++;
        this.clampIndex();
    }

    public void prev()
    {
        this.index--;
        this.clampIndex();
    }

    public void reset()
    {
        this.index = 0;
        this.profile = null;
    }

    private void clampIndex()
    {
        if (this.index < 0) this.index = this.profile.getCount() - 1;
        if (this.index > this.profile.getCount() - 1) this.index = 0;
    }

    /**
     * Save camera profile
     */
    public void save()
    {
        if (this.profile.getFilename().isEmpty()) return;

        this.profile.save();
    }

    /**
     * Load camera profile
     */
    public void load()
    {
        if (this.profile.getFilename().isEmpty()) return;

        Dispatcher.sendToServer(new PacketLoadCameraProfile(this.profile.getFilename()));
    }

    public void goTo(EntityPlayer player) throws CommandException
    {
        AbstractFixture fixture = this.current();
        Position pos = new Position(0, 0, 0, 0, 0);

        Point point = pos.point;
        Angle angle = pos.angle;

        fixture.applyFixture(0, pos);
        player.setPositionAndRotation(point.x, point.y, point.z, angle.yaw, angle.pitch);
    }
}