package org.sirix.io.ram;

import net.openhft.chronicle.bytes.Bytes;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sirix.access.ResourceConfiguration;
import org.sirix.api.PageReadOnlyTrx;
import org.sirix.exception.SirixIOException;
import org.sirix.io.IOStorage;
import org.sirix.io.Reader;
import org.sirix.io.RevisionFileData;
import org.sirix.io.Writer;
import org.sirix.io.bytepipe.ByteHandlePipeline;
import org.sirix.page.PageReference;
import org.sirix.page.RevisionRootPage;
import org.sirix.page.interfaces.Page;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In memory storage. // FIXME: broken
 *
 * @author Johannes Lichtenberger
 */
public final class RAMStorage implements IOStorage {

  /**
   * Storage, mapping a resource to the pageKey/page mapping.
   */
  private final ConcurrentMap<String, ConcurrentMap<Long, Page>> mDataStorage;

  /**
   * Storage, mapping a resource to the revision/revision root page mapping.
   */
  private final ConcurrentMap<String, ConcurrentMap<Integer, RevisionRootPage>> mRevisionRootsStorage;

  /**
   * Mapping pageKey to the page.
   */
  private ConcurrentMap<Long, Page> mResourceFileStorage;

  /**
   * Mapping revision to the page.
   */
  private ConcurrentMap<Integer, RevisionRootPage> mResourceRevisionRootsStorage;

  /**
   * The uber page key.
   */
  private final ConcurrentMap<Integer, Long> mUberPageKey;

  /**
   * {@link ByteHandlePipeline} reference.
   */
  private final ByteHandlePipeline mHandler;

  /**
   * {@link RAMAccess} reference.
   */
  private final RAMAccess mAccess;

  /**
   * Determines if the storage already exists or not.
   */
  private boolean mExists;

  /**
   * The unique page key.
   */
  private long mPageKey;

  /**
   * The resource configuration.
   */
  private final ResourceConfiguration mResourceConfiguration;

  /**
   * Constructor
   *
   * @param resourceConfig {@link ResourceConfiguration} reference
   */
  public RAMStorage(final ResourceConfiguration resourceConfig) {
    mResourceConfiguration = resourceConfig;
    mDataStorage = new ConcurrentHashMap<>();
    mRevisionRootsStorage = new ConcurrentHashMap<>();
    mHandler = resourceConfig.byteHandlePipeline;
    mAccess = new RAMAccess();
    mUberPageKey = new ConcurrentHashMap<>();
    mUberPageKey.put(-1, 0L);
  }

  @Override
  public Writer createWriter() {
    instantiate();

    return mAccess;
  }

  private void instantiate() {
    final String resource = mResourceConfiguration.getResource().getFileName().toString();
    mExists = mDataStorage.containsKey(resource);
    mDataStorage.putIfAbsent(resource, new ConcurrentHashMap<>());
    mResourceFileStorage = mDataStorage.get(resource);
    mRevisionRootsStorage.putIfAbsent(resource, new ConcurrentHashMap<>());
    mResourceRevisionRootsStorage = mRevisionRootsStorage.get(resource);
  }

  @Override
  public Reader createReader() {
    instantiate();

    return mAccess;
  }

  @Override
  public void close() {
  }

  @Override
  public ByteHandlePipeline getByteHandler() {
    return mHandler;
  }

  @Override
  public boolean exists() throws SirixIOException {
    return mExists;
  }

  /**
   * Provides RAM access.
   */
  public class RAMAccess implements Writer {

    @Override
    public Writer truncate() {
      mUberPageKey.clear();
      mResourceFileStorage.clear();
      mExists = false;
      return this;
    }

    @Override
    public Page read(PageReference reference, @Nullable PageReadOnlyTrx pageReadTrx) {
      return mResourceFileStorage.get(reference.getKey());
    }

    @Override
    public PageReference readUberPageReference() {
      final Page page = mResourceFileStorage.get(mUberPageKey.get(-1));
      final PageReference uberPageReference = new PageReference();
      uberPageReference.setKey(-1);
      uberPageReference.setPage(page);
      return uberPageReference;
    }

    @Override
    public Writer write(final PageReadOnlyTrx pageReadOnlyTrx, final PageReference pageReference, final Bytes<ByteBuffer> bufferedBytes) {
      final Page page = pageReference.getPage();
      pageReference.setKey(mPageKey);
      mResourceFileStorage.put(mPageKey++, page);
      mExists = true;
      return this;
    }

    @Override
    public Writer writeUberPageReference(final PageReadOnlyTrx pageReadOnlyTrx, final PageReference pageReference,
        final Bytes<ByteBuffer> bufferedBytes) {
      final Page page = pageReference.getPage();
      pageReference.setKey(mPageKey);
      mResourceFileStorage.put(mPageKey, page);
      mUberPageKey.put(-1, mPageKey++);
      mExists = true;
      return this;
    }

    @Override
    public Instant readRevisionRootPageCommitTimestamp(int revision) {
      // FIXME
      throw new UnsupportedOperationException();
    }

    @Override
    public RevisionFileData getRevisionFileData(int revision) {
      // FIXME
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws SirixIOException {
    }

    @Override
    public Writer truncateTo(PageReadOnlyTrx pageReadOnlyTrx, int revision) {
      // TODO
      return this;
    }

    @Override
    public RevisionRootPage readRevisionRootPage(int revision, PageReadOnlyTrx pageReadTrx) {
      return mResourceRevisionRootsStorage.get(revision);
    }
  }
}
